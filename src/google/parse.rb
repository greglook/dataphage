#!/usr/bin/env ruby

# This script parses a Google Takeout email dump file.

require 'mail'


def fail(msg, code=1)
  STDERR.puts msg
  exit code
end

fail "Usage: #{File.basename($0)} <email.mbox>" if ARGV.empty?

mailbox = ARGV.shift

count = 0

File.open(mailbox) do |file|
  email_lines = nil
  file.each_line do |line|
    # Look for separators like:
    # From 1270598047301532511@xxx Tue May 27 10:31:00 2008
    if line =~ /^From \d+@xxx \w+ \w+ \d+ \d\d:\d\d:\d\d \d\d\d\d/
      if email_lines.nil?
        # First email line.
        email_lines = [line]
      else
        # TODO: parse previous email_lines
        count += 1
        email_lines = [line]
      end
    else
      fail "Expected first line to be email header: #{line}" if email_lines.nil?
      email_lines << line
    end
  end
  # Save last email.
  unless email_lines.nil?
    count += 1
  end
end

puts "Counted #{count} messages in mailbox"
