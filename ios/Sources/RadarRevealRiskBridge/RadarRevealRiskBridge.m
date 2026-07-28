#import "RadarRevealRiskBridge.h"

#if __has_include(<RadarSDK/RadarSDK.h>)
#import <RadarSDK/RadarSDK.h>
#else
@import RadarSDK;
#endif

@implementation RadarRevealRiskBridge

+ (void)revealRiskWithCompletionHandler:(void (^)(NSString *, NSDictionary *_Nullable))completionHandler {
    [Radar revealRiskWithCompletionHandler:^(RadarStatus status, RadarRevealRiskToken *_Nullable token) {
        NSString *statusString = [Radar stringForStatus:status];
        // [token dictionaryValue] is a dynamic message-send — it does NOT require
        // the (unexported) _OBJC_CLASS_$_RadarRevealRiskToken class symbol.
        NSDictionary *dict = token ? [token dictionaryValue] : nil;
        completionHandler(statusString, dict);
    }];
}

@end