var exec = require('cordova/exec');

// exports.pay = function(arg0, success, error) {
    // exec(success, error, "EsayLinkPlugin", "pay", [arg0]);
// };


module.exports = {
	pay: function (paymentInfo, successCallback, errorCallback) {
		 cordova.exec(successCallback, errorCallback, "EsayLinkPlugin", "pay", [paymentInfo]);
	}
}