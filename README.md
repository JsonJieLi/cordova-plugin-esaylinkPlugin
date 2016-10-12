# cordova-plugin-esaylinkPlugin
Cordova易联支付插件
# install form github
$  cordova plugin add https://github.com/JsonJieLi/cordova-plugin-esaylinkPlugin.git--variable PAY_URL={ PAY_URL } --variable NOTIFY_URL={  NOTIFY_URL  } --variable ENVIRONMENT={ 00/01 }


# javascript
     // exports.pay = function(arg0, success, error) {
     // exec(success, error, "EsayLinkPlugin", "pay", [arg0]);
     // };

     module.exports = {
	pay: function (paymentInfo, successCallback, errorCallback) {
		 cordova.exec(successCallback, errorCallback, "EsayLinkPlugin", "pay", [paymentInfo]);
	}
     }

# with Angular (#ionic)
 		var jsonO = eval(successResults);
				$location.path("/member/selfOrderDetail/"+jsonO.MerchOrderId);
				$rootScope.$apply();
			}, function(errorResults){
				log.info(JSON.stringify(errorResults));
				var jsonO = eval(errorResults);
				alert(jsonO.respDesc);
			});
		}else{
			$("#paycoform").attr("action",$rootScope.host+"onlinepay/orderH5.do");
			$("#paycoform").submit();
		}
	}
