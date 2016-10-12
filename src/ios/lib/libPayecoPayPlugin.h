//
//  PayEcoPpi.h
//  PPIDemo
//
//  Created by TangHua on 14-10-27.
//  Copyright (c) 2014年 PayEco. All rights reserved.
//

/*
 *  易联支付库文件: libPayecoPayPlugin.h,libPayecoPayPlugin.a --
 *  商户程序编译设置:
 *  Build Settings -> Linking -> Other Link Flags: -ObjC
 *  Frameworks: Security.framework
                SystemConfiguration.framework
                CFNetwork.framework
                QuartzCore.framework
                CoreGraphics.framework
                CoreLocation.Frameworks
 *
 */

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

//易联支付代理
@protocol PayEcoPpiDelegate <NSObject>

@optional
- (void)payResponse:(NSString *)respJson;       //respJson: 支付返回数据报文(json格式)
@end

@interface PayEcoPpi : NSObject

/*
 *  支付处理(支付界面采用弹出模式)
 *  env:          环境参数 00: 测试环境,01: 生产环境
 *  reqJson:      支付请求数据报文(json格式)
 *  delegate:     易联支付代理,用于处理支付结果
 *  orientation:  易联支付界面的方向。方向参数: 00:横屏 01:竖屏
 */
- (void)startPay:(NSString *)reqJson delegate:(id<PayEcoPpiDelegate>) delegate env:(NSString *)env orientation:(NSString *)orientation;



@end
