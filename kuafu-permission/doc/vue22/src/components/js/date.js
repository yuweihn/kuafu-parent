

export default {
	/**
	 * 功能：实现JS的addDate功能。
	 * 参数：date,时间对象；
	 * 参数：number,数值表达式，表示要添加的时间间隔的个数；
	 * 参数：type,字符串表达式，表示要添加的时间间隔的类型；
	 * 返回：新的时间对象。
	 */
	addDate(date, number, type) {
		if (date == null) {
			return null;
		}
		try {
			date = new Date(date);
		} catch(e) {
			return date;
		}
		switch (type) {
			case "y":
				date.setFullYear(date.getFullYear() + number);
				return date;
			case "q":
				date.setMonth(date.getMonth() + number * 3);
				return date;
			case "m":
				date.setMonth(date.getMonth() + number);
				return date;
			case "w":
				date.setDate(date.getDate() + number * 7);
				return date;
			case "d":
				date.setDate(date.getDate() + number);
				return date;
			case "h":
				date.setHours(date.getHours() + number);
				return date;
			case "m":
				date.setMinutes(date.getMinutes() + number);
				return date;
			case "s":
				date.setSeconds(date.getSeconds() + number);
				return date;
			default:
				date.setDate(date.getDate() + number);
				return date;
		}
	},

	formatDate(date, fmt) {
		if (date == null) {
			return "";
		}
		try {
			date = new Date(date);
		} catch(e) {
			return "";
		}
		if (/(y+)/.test(fmt)) {
			fmt = fmt.replace(RegExp.$1, (date.getFullYear() + '').substr(4 - RegExp.$1.length));
		}
		var o = {
				'M+': date.getMonth() + 1,
				'd+': date.getDate(),
				'H+': date.getHours(),
				'm+': date.getMinutes(),
				's+': date.getSeconds()
		}
		for (var k in o) {
			if (new RegExp("("+ k +")").test(fmt)) {
				var str = o[k] + '';
				fmt = fmt.replace(RegExp.$1, RegExp.$1.length === 1 ? str : padLeftZero(str));
			}
		}
		return fmt;
	}
}

function padLeftZero (str) {
	return ('00' + str).substr(str.length);
}
