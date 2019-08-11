
  Pod::Spec.new do |s|
    s.name = 'CapacitorRadar'
    s.version = '0.0.1'
    s.summary = 'Capacitor plugin for Radar, the location context platform'
    s.license = 'MIT'
    s.homepage = 'radarlabs/capacitor-radar'
    s.author = 'Radar Labs, Inc.'
    s.source = { :git => 'radarlabs/capacitor-radar', :tag => s.version.to_s }
    s.source_files = 'ios/Plugin/**/*.{swift,h,m,c,cc,mm,cpp}'
    s.ios.deployment_target  = '11.0'
    s.dependency 'Capacitor'
    s.dependency 'RadarSDK'
    s.static_framework = true
  end
