#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface RadarRevealRiskBridge : NSObject

+ (void)revealRiskWithCompletionHandler:(void (^)(NSString *status,
                                                  NSDictionary *_Nullable token))completionHandler;

@end

NS_ASSUME_NONNULL_END