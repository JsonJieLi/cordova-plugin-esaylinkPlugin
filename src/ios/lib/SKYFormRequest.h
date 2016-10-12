//
//  SKYFormRequest.h
//  PPIDemo
//
//  Created by TangHua on 14-10-24.
//  Copyright (c) 2014年 PayEco. All rights reserved.
//

#import <Foundation/Foundation.h>

// -1001: request timeout
// -1002: unsupported URL
// -1003: cannot find host
// -1004: cannot connect to host
// -1005: network connection lost
// -1006: DNS lookup failed
// -1008: resource unavailable
// -1009: not connected to internet


//Form请求代理
@protocol SKYFormRequestDelegate <NSObject>
@optional
- (void)onRequestFail:(NSString *)errCode errMsg:(NSString *)errMsg;
- (void)onRequestSucc:(NSString *)strReponse;
@end

@interface SKYFormRequest : NSObject

@property (nonatomic, assign) id<SKYFormRequestDelegate> requestDelegate;

- (id)init:(NSString *)url timeOut:(NSTimeInterval)timeOut;//初始化处理
- (id)init:(NSString *)url srcFilePath:(NSString *)filePath params:(NSDictionary *)params timeOut:(NSTimeInterval)timeOut;//上传文件初始化处理
- (void)addParam:(NSString *)key value:(NSString *)value;//添加http参数
- (NSString *)paramValueForKey:(NSString *)key;          //获取参数value
- (void)httpPostAsyn;//异步请求处理
- (void)httpPostUploadAsyn;//异步上传处理

@end
