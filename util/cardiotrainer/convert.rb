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
# {:vault.entity/source-id "ebd43dc2f4a55d4c9a455de848fd178ae82363d0"
#  :time/at #inst "2014-06-16T14:52:00-0700"
#  :time/duration #phys/q [1670 s]
#  :exercise/type :exercise.type/running
#  :exercise/calories 273
#  :exercise/distance #phys/q [4.09 km]
#  :geo.track/min-speed #phys/q [0.0 kph]
#  :geo.track/avg-speed #phys/q [8.82 kph]
#  :geo.track/max-speed #phys/q [14.66 kph]
#  :geo.track/climb #phys/q [165.05 m]
#  :geo/track #vault/ref "sha256:..."}
#
# Track blob:
# {:vault/type :geo/track
#  :track #geo/coordinates
#  [[47.605484999999994 -122.33734199999999 13.0]
#   [47.605543 -122.33739 8.0]
#   ...]}

require 'edn'
require 'json'
require 'time'


KPH = {~"*" => 1000/3600.0, ~"m" => 1, ~"s" => -1}


class PhysicalQuantity
  attr_reader :magnitude, :unit

  def initialize(m, u)
    @magnitude = m
    @unit = u
  end

  def to_edn
    EDN.tagout("physical/quantity", [@magnitude, @unit])
  end
end


class GeoTrack
  attr_reader :coordinates

  def initialize(coords)
    @coordinates = coords
  end

  def to_edn
    EDN.tagout("geo/track", @coordinates)
  end
end



##### PROCESSING #####

data = JSON.load(STDIN)

duration = data["duration"].split(':').reverse.map(&:to_i).zip([1, 60, 3600]).map {|v, s| v*s }.reduce(:+)

# TODO: these coordinates seem to occasionally omit the latitude?
# Assume that means it hasn't changed.
track_coords = data["trackInterval"].split(',').map(&:to_f).each_slice(3).to_a

last_coord = track_coords.first
track_coords.each do |coord|
  if coord.count == 3
    displacement = (coord[0] - last_coord[0]).abs + (coord[1] - last_coord[1]).abs
    STDERR.puts "Unexpectedly high displacement! #{last_coord} -> #{coord} (#{displacement})" if displacement > 10.0
  else
    STDERR.puts "Track coordinate has fewer than three elements: #{coord}"
  end
  last_coord = coord
end


track = {
  ~":vault.entity/source-id" => data["trackIdSignature"],
  ~":time/at" => Time.parse(data["date"]),
  ~":time/duration" => PhysicalQuantity.new(duration, ~"s"),
  ~":exercise/type" => ~":exercise.type/#{data["exercise_type"].split('_').last}",
  ~":exercise/calories" => data["calories"],
  ~":distance" => PhysicalQuantity.new(data["distance"], ~"km"),
  ~":geo.track/min-speed" => PhysicalQuantity.new(data["minSpeed"], KPH),
  ~":geo.track/avg-speed" => PhysicalQuantity.new(data["avgSpeed"], KPH),
  ~":geo.track/max-speed" => PhysicalQuantity.new(data["maxSpeed"], KPH),
  ~":geo.track/climb" => PhysicalQuantity.new(data["climb"], ~"m"),
  ~":geo/track" => GeoTrack.new(track_coords),
}

puts track.to_edn
