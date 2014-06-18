require 'json'
require 'mechanize'


TRACKS_URL = "http://www.noom.com/cardiotrainer/tracks.php"


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



# TODO: read config file, load tracks
