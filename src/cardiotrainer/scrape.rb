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
require 'yaml'


TRACKS_URL = "http://www.noom.com/cardiotrainer/tracks.php"



##### CONFIGURATION #####

$options = {
  verbose: true,
}

OptionParser.new do |opts|
  opts.banner = "Usage: #{File.basename($0)} [options] <config>"
  opts.separator ""
  opts.separator "Options:"
  opts.on('-h', '--help', "Display usage information") { print opts; exit }
end.parse!

def fail(msg, code=1)
  STDERR.puts msg
  exit code
end

def log(msg)
  puts msg if $options[:verbose]
end

fail opts if ARGV.empty?

$config = File.open(ARGV.first) {|file| YAML.load(file) }

fail "No access code found in config file #{ARGV.first}" unless $config["access_code"]


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

  $agent.post(TRACKS_URL, {access: access, code: code})
end


# Loads the tracks page with some offset. Returns the resulting page.
def load_tracks(offset=0)
  $agent.get(TRACKS_URL, {offset: offset})
end


# Scrapes track data as JSON out of a script on the tracks page.
def scrape_tracks(page)
  scripts = page.search("/html/head/script[not(@src)]/text()").map(&:to_s)
  tracks_js = scripts.find {|s| /^\s*var trackData =/ === s }
  tracks_json = tracks_js[tracks_js.index('{')..tracks_js.rindex('}')]
  JSON.parse(tracks_json)
end


# Track JSON data looks like this:
# tracks = {"773581901" => {...}, "773209895" => {...}, ...}
#
# Keys in a track map:
# ["trackIdSignature", "duration", "distance", "date", "minSpeed", "maxSpeed", "avgSpeed", "climb", "calories", "exercise_type", "track_name", "trackInterval"]


# TODO: implement
# - look in output directory for marker file of last download
# - log in, scrape tracks off page
# - parse tracks, storing by unique id
# - keep note of most recent track date
# - continue loading tracks with offset until hitting date mark
# - write new date mark (and associated unique id) to marker file
