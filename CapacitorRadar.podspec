
  Pod::Spec.new do |s|
    package = JSON.parse(File.read(File.join(File.dirname(__FILE__), 'package.json')))
    s.name = 'CapacitorRadar'
    s.version = package['version']
    s.summary = package['description']
    s.license = package['license']
    s.homepage = 'radarlabs/capacitor-radar'
    s.author = 'Radar Labs, Inc.'
    s.source = { :git => 'radarlabs/capacitor-radar', :tag => s.version.to_s }
    s.source_files = 'ios/Plugin/**/*.{swift,h,m,c,cc,mm,cpp}'
    s.ios.deployment_target  = '13.0'
    s.dependency 'Capacitor'
    s.dependency 'RadarSDK', '~> 3.23.2'
    s.static_framework = true
  end
