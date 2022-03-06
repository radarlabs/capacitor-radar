#!/usr/bin/env ruby

print <<HEAD
#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

CAP_PLUGIN(RadarPlugin, "Radar",
HEAD

File.open("./Plugin.swift").each_line { | line |
    match = line.match(/@objc func (.*)\(_ call: CAPPluginCall\)/)
    
    if match
        puts "    CAP_PLUGIN_METHOD(" + match[1] + ", CAPPluginReturnPromise);"
    end
}

puts ")"

