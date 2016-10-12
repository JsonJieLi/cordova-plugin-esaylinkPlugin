//
//  EsayLinkPlugin.h
//  HelloCordova
//
//  Created by xiaohaijian on 16/9/9.
//
//

/********* myplugin.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>
#import "libPayecoPayPlugin.h"
#import "PPIHudView.h"
#import "SKYFormRequest.h"

//商户服务器下单地址，此地址为商户平台测试环境下单地址，商户接入需改为商户自己的服务器下单地址
#define URL_PAY_ORDER @"https://test.hjw.com/onlinepay/orderAPP.do"

//模拟通知商户地址，建议在接收到支付成功结果时，通知商户服务器
#define URL_PAY_NOTIFY @"https://test.hjw.com/onlinepay/aSyncReceivePaycoAPP.do"


@interface EsayLinkPlugin : CDVPlugin <SKYFormRequestDelegate, UITextFieldDelegate,PayEcoPpiDelegate>
{
    
    PPIHudView *hud;                 //加载控件   （商户根据自己的情况，使用自己的加载控件）
    SKYFormRequest *formRequest;     //网络请求类 （商户根据自己的情况，使用自己的网络请求类）
    PayEcoPpi *payEcoPpi;            //易联支付类
    NSString *respJson;              //易联支付SDK返回的json信息
    NSString *tradeCode;             //只是用于区分下单过程和通知商户服务器过程
}

@property(nonatomic,strong)NSDictionary * echoDict;
@property(nonatomic,strong)NSString *echo;
@property(nonatomic,strong)NSString *payUrl;
@property(nonatomic,strong)NSString *notifyUrl;
@property(nonatomic,strong)NSString *environment;
@property(nonatomic,strong)NSString *currentCallbackId;


- (void)pay:(CDVInvokedUrlCommand*)command;

@end