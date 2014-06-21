#!/usr/bin/env ruby

# This script converts downloaded track JSON into canonical EDN.
#
# JSON:
# {
#   "trackIdSignature" => "ebd43dc2f4a55d4c9a455de848fd178ae82363d0",
#   "duration" => "00:27:50",
#   "distance" => 4.09,
#   "date" => "Monday<br/>Jun.16, 2014<br/>02:52 pm",
#   "minSpeed" => 0,
#   "maxSpeed" => 14.66,
#   "avgSpeed" => 8.82,
#   "climb" => 165.05,
#   "calories" => 273,
#   "exercise_type" => "exercise_type_walking",
#   "track_name" => nil,
#   "trackInterval" => "47.605484999999994,-122.33734199999999,13.0,47.605543,-122.33739,8.0,...",
# }
#
# EDN:
# {:vault.entity/id "ebd43dc2f4a55d4c9a455de848fd178ae82363d0"
#  :time/at #inst "2014-06-16T14:52:00-0700"
#  :time/duration #phys/q [1670 s]
#  :distance #phys/q [4.09 km]
#  :geo.track/min-speed #phys/q [0.0 {* 5/18 m 1 s -1}]
#  :geo.track/avg-speed #phys/q [8.82 {* 5/18 m 1 s -1}]
#  :geo.track/max-speed #phys/q [14.66 {* 5/18 m 1 s -1}]
#  :geo.track/climb #phys/q [165.05 m]
#  :calories 273
#  :exercise/type :exercise.type/walking
#  :geo/track [...]}

require 'json'
require 'time'


def fail(msg, code=1)
  STDERR.puts msg
  exit code
end

def log(msg)
  puts "%s %s" % [Time.now.strfmt("HH:MM:SS"), msg]
end

fail "Usage: #{File.basename($0)} <track.json> <track.edn>" unless ARGV.count == 2

$input = ARGV.shift
$output = ARGV.shift

log "Reading input file #{$input}"

track_json = File.open($input) do |file| JSON.load(file) end

# TODO: convert
