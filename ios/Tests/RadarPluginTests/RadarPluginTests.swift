import XCTest
import Capacitor
@testable import RadarPlugin

class RadarPluginTests: XCTestCase {
    func testPluginRegistersRadarMethods() {
        let plugin = RadarPlugin()
        XCTAssertEqual(plugin.jsName, "Radar")
        XCTAssertTrue(plugin.pluginMethods.contains { $0.name == "trackOnce" })
    }
}