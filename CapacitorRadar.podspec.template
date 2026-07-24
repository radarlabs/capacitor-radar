
  Pod::Spec.new do |s|
    package = JSON.parse(File.read(File.join(File.dirname(__FILE__), 'package.json')))
    s.name = 'CapacitorRadar'
    s.version = package['version']
    s.summary = package['description']
    s.license = package['license']
    s.homepage = 'radarlabs/capacitor-radar'
    s.author = 'Radar Labs, Inc.'
    s.source = { :git => 'radarlabs/capacitor-radar', :tag => s.version.to_s }
    s.source_files = 'ios/Sources/RadarPlugin/**/*.swift'
    s.ios.deployment_target = '15.0'
    s.dependency 'Capacitor'
    s.vendored_frameworks = 'ios/Frameworks/RadarSDK.xcframework', 'ios/Frameworks/RadarSDKFraud.xcframework'
    s.static_framework = true
  end
