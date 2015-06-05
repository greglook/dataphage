#!/usr/bin/env ruby

# This script scrapes track data from CardioTrainer logs. The configuration file
# should be a YAML document containing the app's access code, found in the
# settings screen:
#
#     ---
#     access_code: "ABCD1234"
#

require 'json'
require 'mechanize'
require 'optparse'
require 'time'
require 'yaml'


TRACKS_URL = "http://www.noom.com/cardiotrainer/tracks.php"



##### CONFIGURATION #####

$options = {
  verbose: false,
  config_file: nil,
  data_dir: nil,
}

# TODO: 'ignore-mark' option to force-fetch all data
# TODO: 'rebuild-mark' to read existing data

opts = OptionParser.new do |opts|
  opts.banner = "Usage: #{File.basename($0)} [options] <config file> <data dir>"
  opts.separator ""
  opts.separator "Options:"
  opts.on('-v', '--verbose', "Show extra process information") { $options[:verbose] = true }
  opts.on('-h', '--help', "Display usage information") { print opts; exit }
end
opts.parse!

def fail(msg, code=1)
  STDERR.puts msg
  exit code
end

def log(msg)
  puts msg if $options[:verbose]
end

fail opts if ARGV.count < 2

$options[:config_file] = ARGV.shift
$options[:data_dir] = ARGV.shift

fail opts unless ARGV.empty?
fail "Data directory #{$options[:data_dir]} does not exist!" unless File.directory? $options[:data_dir]

$config = File.open($options[:config_file]) {|file| YAML.load(file) }
fail "No access code found in config file #{$options[:config_file]}" unless $config["access_code"]



##### SCRAPING #####

$agent = Mechanize.new do |agent|
  # TODO: randomize UA
  agent.user_agent_alias = "Mac Firefox"
end


# Logs into the Cardiotrainer dashboard with the given 8-character access code.
# Returns the resulting page.
def login(access_code)
  raise "Access code must be 8 characters" unless access_code.length == 8
  access = access_code[0..3]
  code   = access_code[4..7]

  log "Logging into Cardiotrainer tracks page"

  $agent.post(TRACKS_URL, {access: access, code: code})
end


# Loads the tracks page with some offset. Returns the resulting page.
def load_tracks(offset=0)
  log "Fetching tracks at offset #{offset}"
  $agent.get(TRACKS_URL, {offset: offset})
end


# Scrapes track data as JSON out of a script on the tracks page.
def scrape_tracks(page)
  scripts = page.search("/html/head/script[not(@src)]/text()").map(&:to_s)
  tracks_js = scripts.find {|s| /^\s*var trackData =/ === s }
  if tracks_js
    tracks_json = tracks_js[tracks_js.index('{')..tracks_js.rindex('}')]
    JSON.parse(tracks_json)
  end
end



##### PROCESSING #####

# data/cardiotrainer/tracks/<trackId>.json
# data/cardiotrainer/marks.yml

# marks.yml contains:
# ---
# timestamp: 2014-06-18 22:33:14 UTC  (date of last-fetched piece of data)
# track: <trackId>                    (id of last-fetched track)

marks_file = File.join($options[:data_dir], 'marks.yml')
tracks_dir = File.join($options[:data_dir], 'tracks')
Dir.mkdir(tracks_dir) unless File.directory? tracks_dir

mark_timestamp = nil
mark_track = nil

if File.exist? marks_file
  marks = File.open(marks_file) {|f| YAML.load(f) }
  mark_timestamp = Time.parse(marks["timestamp"])
  mark_track = marks["track"]
end

if mark_timestamp || mark_track
  puts "Scraping until #{mark_timestamp} from track #{mark_track}"
else
  puts "Scraping all track data (no marks found)"
end

last_timestamp = nil
last_track = nil

page = login $config["access_code"]
offset = 0

while last_timestamp.nil? || mark_timestamp.nil? || last_timestamp >= mark_timestamp
  tracks = scrape_tracks page

  if tracks.nil? || tracks.empty?
    log "No more tracks available"
    break
  else
    log "Processing #{tracks.count} tracks"
  end

  # Track JSON data looks like this:
  # tracks = {"773581901" => {...}, "773209895" => {...}, ...}
  tracks.each do |id, track|
    track_time = Time.parse(track["date"]).utc
    if last_timestamp.nil? || last_timestamp < track_time
      last_timestamp = track_time
      last_track = id
    end

    track_path = File.join(tracks_dir, "#{id}.json")
    if File.exist? track_path
      log "Already downloaded track #{id}, skipping..."
    else
      File.open(track_path, 'w') do |file|
        file.write(JSON.dump(track))
      end
    end
  end

  delay = 10.0*rand
  log "Sleeping for #{delay} seconds"
  sleep delay

  offset += tracks.count
  page = load_tracks offset
end

# Write marks file.
File.open(marks_file, 'w') do |file|
  marks = {
    "timestamp" => last_timestamp,
    "track" => last_track,
  }
  file.write(YAML.dump(marks))
end
