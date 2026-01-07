# OpenAPI definition


**简介**:OpenAPI definition


**HOST**:http://localhost:8080


**联系人**:


**Version**:v0


**接口路径**:/v3/api-docs/default


[TOC]






# 地图服务


## 路径规划


**接口地址**:`/api/map/direction`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>获取两点间的路径规划信息</p>



**请求示例**:


```javascript
{
  "fromLatitude": 39.984154,
  "fromLongitude": 116.30749,
  "toLatitude": 39.998766,
  "toLongitude": 116.474977,
  "mode": "walking"
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|directionRequest|路径规划请求|body|true|DirectionRequest|DirectionRequest|
|&emsp;&emsp;fromLatitude|起点纬度||true|number(double)||
|&emsp;&emsp;fromLongitude|起点经度||true|number(double)||
|&emsp;&emsp;toLatitude|终点纬度||true|number(double)||
|&emsp;&emsp;toLongitude|终点经度||true|number(double)||
|&emsp;&emsp;mode|出行方式: walking-步行, driving-驾车, transit-公交||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultDirectionResult|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||DirectionResult|DirectionResult|
|&emsp;&emsp;status||integer(int32)||
|&emsp;&emsp;message||string||
|&emsp;&emsp;result||Result|Result|
|&emsp;&emsp;&emsp;&emsp;routes||array|Route|
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;distance||integer||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;duration||integer||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;direction||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;polyline||array|number|
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;steps||array|Step|
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;instruction||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;distance||integer||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;duration||integer||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;roadName||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;dirDesc||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;polyline||string||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"status": 0,
		"message": "",
		"result": {
			"routes": [
				{
					"distance": 0,
					"duration": 0,
					"direction": "",
					"polyline": [],
					"steps": [
						{
							"instruction": "",
							"distance": 0,
							"duration": 0,
							"roadName": "",
							"dirDesc": "",
							"polyline": ""
						}
					]
				}
			]
		}
	},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 距离计算


**接口地址**:`/api/map/distance`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>计算两点间的距离和预估时间</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|fromLatitude|起点纬度|query|true|number(double)||
|fromLongitude|起点经度|query|true|number(double)||
|toLatitude|终点纬度|query|true|number(double)||
|toLongitude|终点经度|query|true|number(double)||
|mode|出行方式: walking-步行, driving-驾车|query|false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultDistanceResult|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||DistanceResult|DistanceResult|
|&emsp;&emsp;status||integer(int32)||
|&emsp;&emsp;message||string||
|&emsp;&emsp;result||Result|Result|
|&emsp;&emsp;&emsp;&emsp;routes||array|Route|
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;distance||integer||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;duration||integer||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;direction||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;polyline||array|number|
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;steps||array|Step|
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;instruction||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;distance||integer||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;duration||integer||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;roadName||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;dirDesc||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;polyline||string||
|&emsp;&emsp;elements||array|Element|
|&emsp;&emsp;&emsp;&emsp;distance||integer||
|&emsp;&emsp;&emsp;&emsp;duration||integer||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"status": 0,
		"message": "",
		"result": {
			"routes": [
				{
					"distance": 0,
					"duration": 0,
					"direction": "",
					"polyline": [],
					"steps": [
						{
							"instruction": "",
							"distance": 0,
							"duration": 0,
							"roadName": "",
							"dirDesc": "",
							"polyline": ""
						}
					]
				}
			]
		},
		"elements": [
			{
				"distance": 0,
				"duration": 0
			}
		]
	},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 地址解析


**接口地址**:`/api/map/geocoder`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>根据地址字符串获取经纬度</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|address|地址|query|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultGeocoderResult|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||GeocoderResult|GeocoderResult|
|&emsp;&emsp;status||integer(int32)||
|&emsp;&emsp;message||string||
|&emsp;&emsp;result||Result|Result|
|&emsp;&emsp;&emsp;&emsp;routes||array|Route|
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;distance||integer||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;duration||integer||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;direction||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;polyline||array|number|
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;steps||array|Step|
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;instruction||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;distance||integer||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;duration||integer||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;roadName||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;dirDesc||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;polyline||string||
|&emsp;&emsp;resultData||ResultData|ResultData|
|&emsp;&emsp;&emsp;&emsp;address||string||
|&emsp;&emsp;&emsp;&emsp;location||Location|Location|
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;lat||number||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;lng||number||
|&emsp;&emsp;&emsp;&emsp;addressComponent||AddressComponent|AddressComponent|
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;nation||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;province||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;city||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;district||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;street||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;streetNumber||string||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"status": 0,
		"message": "",
		"result": {
			"routes": [
				{
					"distance": 0,
					"duration": 0,
					"direction": "",
					"polyline": [],
					"steps": [
						{
							"instruction": "",
							"distance": 0,
							"duration": 0,
							"roadName": "",
							"dirDesc": "",
							"polyline": ""
						}
					]
				}
			]
		},
		"resultData": {
			"address": "",
			"location": {
				"lat": 0,
				"lng": 0
			},
			"addressComponent": {
				"nation": "",
				"province": "",
				"city": "",
				"district": "",
				"street": "",
				"streetNumber": ""
			}
		}
	},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 逆地址解析


**接口地址**:`/api/map/geocoder/reverse`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>根据经纬度获取详细地址信息</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|latitude|纬度|query|true|number(double)||
|longitude|经度|query|true|number(double)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultGeocoderResult|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||GeocoderResult|GeocoderResult|
|&emsp;&emsp;status||integer(int32)||
|&emsp;&emsp;message||string||
|&emsp;&emsp;result||Result|Result|
|&emsp;&emsp;&emsp;&emsp;routes||array|Route|
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;distance||integer||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;duration||integer||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;direction||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;polyline||array|number|
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;steps||array|Step|
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;instruction||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;distance||integer||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;duration||integer||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;roadName||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;dirDesc||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;polyline||string||
|&emsp;&emsp;resultData||ResultData|ResultData|
|&emsp;&emsp;&emsp;&emsp;address||string||
|&emsp;&emsp;&emsp;&emsp;location||Location|Location|
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;lat||number||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;lng||number||
|&emsp;&emsp;&emsp;&emsp;addressComponent||AddressComponent|AddressComponent|
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;nation||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;province||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;city||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;district||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;street||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;streetNumber||string||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"status": 0,
		"message": "",
		"result": {
			"routes": [
				{
					"distance": 0,
					"duration": 0,
					"direction": "",
					"polyline": [],
					"steps": [
						{
							"instruction": "",
							"distance": 0,
							"duration": 0,
							"roadName": "",
							"dirDesc": "",
							"polyline": ""
						}
					]
				}
			]
		},
		"resultData": {
			"address": "",
			"location": {
				"lat": 0,
				"lng": 0
			},
			"addressComponent": {
				"nation": "",
				"province": "",
				"city": "",
				"district": "",
				"street": "",
				"streetNumber": ""
			}
		}
	},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


# 订单模块


## 订单详情


**接口地址**:`/api/order/{id}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id||path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultCourseOrder|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||CourseOrder|CourseOrder|
|&emsp;&emsp;id||integer(int64)||
|&emsp;&emsp;orderNo||string||
|&emsp;&emsp;parentId||integer(int64)||
|&emsp;&emsp;studentId||integer(int64)||
|&emsp;&emsp;tutorId||integer(int64)||
|&emsp;&emsp;tutorProfileId||integer(int64)||
|&emsp;&emsp;demandId||integer(int64)||
|&emsp;&emsp;subject||string||
|&emsp;&emsp;grade||string||
|&emsp;&emsp;teachMode||integer(int32)||
|&emsp;&emsp;unitPrice||number||
|&emsp;&emsp;totalHours||integer(int32)||
|&emsp;&emsp;totalAmount||number||
|&emsp;&emsp;serviceFee||number||
|&emsp;&emsp;tutorAmount||number||
|&emsp;&emsp;usedHours||integer(int32)||
|&emsp;&emsp;status||integer(int32)||
|&emsp;&emsp;payTime||string(date-time)||
|&emsp;&emsp;payType||integer(int32)||
|&emsp;&emsp;payTradeNo||string||
|&emsp;&emsp;cancelReason||string||
|&emsp;&emsp;remark||string||
|&emsp;&emsp;createTime||string(date-time)||
|&emsp;&emsp;updateTime||string(date-time)||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"id": 0,
		"orderNo": "",
		"parentId": 0,
		"studentId": 0,
		"tutorId": 0,
		"tutorProfileId": 0,
		"demandId": 0,
		"subject": "",
		"grade": "",
		"teachMode": 0,
		"unitPrice": 0,
		"totalHours": 0,
		"totalAmount": 0,
		"serviceFee": 0,
		"tutorAmount": 0,
		"usedHours": 0,
		"status": 0,
		"payTime": "",
		"payType": 0,
		"payTradeNo": "",
		"cancelReason": "",
		"remark": "",
		"createTime": "",
		"updateTime": ""
	},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 取消订单


**接口地址**:`/api/order/{id}/cancel`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id||path|true|integer(int64)||
|reason||query|false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultVoid|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 完成订单


**接口地址**:`/api/order/{id}/complete`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id||path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultVoid|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 教员确认开课


**接口地址**:`/api/order/{id}/start`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id||path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultVoid|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 创建订单


**接口地址**:`/api/order/create`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "studentId": 0,
  "tutorProfileId": 0,
  "demandId": 0,
  "subject": "",
  "grade": "",
  "teachMode": 0,
  "unitPrice": 0,
  "totalHours": 0,
  "remark": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|createOrderRequest|CreateOrderRequest|body|true|CreateOrderRequest|CreateOrderRequest|
|&emsp;&emsp;studentId|||true|integer(int64)||
|&emsp;&emsp;tutorProfileId|||true|integer(int64)||
|&emsp;&emsp;demandId|||false|integer(int64)||
|&emsp;&emsp;subject|||true|string||
|&emsp;&emsp;grade|||true|string||
|&emsp;&emsp;teachMode|||true|integer(int32)||
|&emsp;&emsp;unitPrice|||true|number||
|&emsp;&emsp;totalHours|||true|integer(int32)||
|&emsp;&emsp;remark|||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultLong|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||integer(int64)|integer(int64)|
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": 0,
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 家长订单列表


**接口地址**:`/api/order/parent/list`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|status||query|false|integer(int32)||
|page||query|false|integer(int32)||
|size||query|false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultIPageCourseOrder|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||IPageCourseOrder|IPageCourseOrder|
|&emsp;&emsp;size||integer(int64)||
|&emsp;&emsp;total||integer(int64)||
|&emsp;&emsp;records||array|CourseOrder|
|&emsp;&emsp;&emsp;&emsp;id||integer||
|&emsp;&emsp;&emsp;&emsp;orderNo||string||
|&emsp;&emsp;&emsp;&emsp;parentId||integer||
|&emsp;&emsp;&emsp;&emsp;studentId||integer||
|&emsp;&emsp;&emsp;&emsp;tutorId||integer||
|&emsp;&emsp;&emsp;&emsp;tutorProfileId||integer||
|&emsp;&emsp;&emsp;&emsp;demandId||integer||
|&emsp;&emsp;&emsp;&emsp;subject||string||
|&emsp;&emsp;&emsp;&emsp;grade||string||
|&emsp;&emsp;&emsp;&emsp;teachMode||integer||
|&emsp;&emsp;&emsp;&emsp;unitPrice||number||
|&emsp;&emsp;&emsp;&emsp;totalHours||integer||
|&emsp;&emsp;&emsp;&emsp;totalAmount||number||
|&emsp;&emsp;&emsp;&emsp;serviceFee||number||
|&emsp;&emsp;&emsp;&emsp;tutorAmount||number||
|&emsp;&emsp;&emsp;&emsp;usedHours||integer||
|&emsp;&emsp;&emsp;&emsp;status||integer||
|&emsp;&emsp;&emsp;&emsp;payTime||string||
|&emsp;&emsp;&emsp;&emsp;payType||integer||
|&emsp;&emsp;&emsp;&emsp;payTradeNo||string||
|&emsp;&emsp;&emsp;&emsp;cancelReason||string||
|&emsp;&emsp;&emsp;&emsp;remark||string||
|&emsp;&emsp;&emsp;&emsp;createTime||string||
|&emsp;&emsp;&emsp;&emsp;updateTime||string||
|&emsp;&emsp;current||integer(int64)||
|&emsp;&emsp;pages||integer(int64)||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"size": 0,
		"total": 0,
		"records": [
			{
				"id": 0,
				"orderNo": "",
				"parentId": 0,
				"studentId": 0,
				"tutorId": 0,
				"tutorProfileId": 0,
				"demandId": 0,
				"subject": "",
				"grade": "",
				"teachMode": 0,
				"unitPrice": 0,
				"totalHours": 0,
				"totalAmount": 0,
				"serviceFee": 0,
				"tutorAmount": 0,
				"usedHours": 0,
				"status": 0,
				"payTime": "",
				"payType": 0,
				"payTradeNo": "",
				"cancelReason": "",
				"remark": "",
				"createTime": "",
				"updateTime": ""
			}
		],
		"current": 0,
		"pages": 0
	},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 支付订单


**接口地址**:`/api/order/pay`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "orderId": 0,
  "payType": 0,
  "payPassword": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|payOrderRequest|PayOrderRequest|body|true|PayOrderRequest|PayOrderRequest|
|&emsp;&emsp;orderId|||true|integer(int64)||
|&emsp;&emsp;payType|||true|integer(int32)||
|&emsp;&emsp;payPassword|||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultVoid|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 教员订单列表


**接口地址**:`/api/order/tutor/list`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|status||query|false|integer(int32)||
|page||query|false|integer(int32)||
|size||query|false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultIPageCourseOrder|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||IPageCourseOrder|IPageCourseOrder|
|&emsp;&emsp;size||integer(int64)||
|&emsp;&emsp;total||integer(int64)||
|&emsp;&emsp;records||array|CourseOrder|
|&emsp;&emsp;&emsp;&emsp;id||integer||
|&emsp;&emsp;&emsp;&emsp;orderNo||string||
|&emsp;&emsp;&emsp;&emsp;parentId||integer||
|&emsp;&emsp;&emsp;&emsp;studentId||integer||
|&emsp;&emsp;&emsp;&emsp;tutorId||integer||
|&emsp;&emsp;&emsp;&emsp;tutorProfileId||integer||
|&emsp;&emsp;&emsp;&emsp;demandId||integer||
|&emsp;&emsp;&emsp;&emsp;subject||string||
|&emsp;&emsp;&emsp;&emsp;grade||string||
|&emsp;&emsp;&emsp;&emsp;teachMode||integer||
|&emsp;&emsp;&emsp;&emsp;unitPrice||number||
|&emsp;&emsp;&emsp;&emsp;totalHours||integer||
|&emsp;&emsp;&emsp;&emsp;totalAmount||number||
|&emsp;&emsp;&emsp;&emsp;serviceFee||number||
|&emsp;&emsp;&emsp;&emsp;tutorAmount||number||
|&emsp;&emsp;&emsp;&emsp;usedHours||integer||
|&emsp;&emsp;&emsp;&emsp;status||integer||
|&emsp;&emsp;&emsp;&emsp;payTime||string||
|&emsp;&emsp;&emsp;&emsp;payType||integer||
|&emsp;&emsp;&emsp;&emsp;payTradeNo||string||
|&emsp;&emsp;&emsp;&emsp;cancelReason||string||
|&emsp;&emsp;&emsp;&emsp;remark||string||
|&emsp;&emsp;&emsp;&emsp;createTime||string||
|&emsp;&emsp;&emsp;&emsp;updateTime||string||
|&emsp;&emsp;current||integer(int64)||
|&emsp;&emsp;pages||integer(int64)||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"size": 0,
		"total": 0,
		"records": [
			{
				"id": 0,
				"orderNo": "",
				"parentId": 0,
				"studentId": 0,
				"tutorId": 0,
				"tutorProfileId": 0,
				"demandId": 0,
				"subject": "",
				"grade": "",
				"teachMode": 0,
				"unitPrice": 0,
				"totalHours": 0,
				"totalAmount": 0,
				"serviceFee": 0,
				"tutorAmount": 0,
				"usedHours": 0,
				"status": 0,
				"payTime": "",
				"payType": 0,
				"payTradeNo": "",
				"cancelReason": "",
				"remark": "",
				"createTime": "",
				"updateTime": ""
			}
		],
		"current": 0,
		"pages": 0
	},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


# 家长模块


## 添加学生


**接口地址**:`/api/parent/student`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "id": 0,
  "studentName": "",
  "gender": 0,
  "grade": "",
  "schoolName": "",
  "weakSubjects": [],
  "studyDesc": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|studentRequest|StudentRequest|body|true|StudentRequest|StudentRequest|
|&emsp;&emsp;id|||false|integer(int64)||
|&emsp;&emsp;studentName|||true|string||
|&emsp;&emsp;gender|||true|integer(int32)||
|&emsp;&emsp;grade|||true|string||
|&emsp;&emsp;schoolName|||false|string||
|&emsp;&emsp;weakSubjects|||false|array|string|
|&emsp;&emsp;studyDesc|||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultLong|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||integer(int64)|integer(int64)|
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": 0,
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 更新学生信息


**接口地址**:`/api/parent/student`


**请求方式**:`PUT`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "id": 0,
  "studentName": "",
  "gender": 0,
  "grade": "",
  "schoolName": "",
  "weakSubjects": [],
  "studyDesc": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|studentRequest|StudentRequest|body|true|StudentRequest|StudentRequest|
|&emsp;&emsp;id|||false|integer(int64)||
|&emsp;&emsp;studentName|||true|string||
|&emsp;&emsp;gender|||true|integer(int32)||
|&emsp;&emsp;grade|||true|string||
|&emsp;&emsp;schoolName|||false|string||
|&emsp;&emsp;weakSubjects|||false|array|string|
|&emsp;&emsp;studyDesc|||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultVoid|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 获取学生详情


**接口地址**:`/api/parent/student/{id}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id||path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultParentStudent|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||ParentStudent|ParentStudent|
|&emsp;&emsp;id||integer(int64)||
|&emsp;&emsp;parentId||integer(int64)||
|&emsp;&emsp;studentName||string||
|&emsp;&emsp;gender||integer(int32)||
|&emsp;&emsp;grade||string||
|&emsp;&emsp;schoolName||string||
|&emsp;&emsp;weakSubjects||string||
|&emsp;&emsp;studyDesc||string||
|&emsp;&emsp;createTime||string(date-time)||
|&emsp;&emsp;updateTime||string(date-time)||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"id": 0,
		"parentId": 0,
		"studentName": "",
		"gender": 0,
		"grade": "",
		"schoolName": "",
		"weakSubjects": "",
		"studyDesc": "",
		"createTime": "",
		"updateTime": ""
	},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 删除学生


**接口地址**:`/api/parent/student/{id}`


**请求方式**:`DELETE`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id||path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultVoid|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 获取我的学生列表


**接口地址**:`/api/parent/students`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


暂无


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultListParentStudent|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||array|ParentStudent|
|&emsp;&emsp;id||integer(int64)||
|&emsp;&emsp;parentId||integer(int64)||
|&emsp;&emsp;studentName||string||
|&emsp;&emsp;gender||integer(int32)||
|&emsp;&emsp;grade||string||
|&emsp;&emsp;schoolName||string||
|&emsp;&emsp;weakSubjects||string||
|&emsp;&emsp;studyDesc||string||
|&emsp;&emsp;createTime||string(date-time)||
|&emsp;&emsp;updateTime||string(date-time)||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": [
		{
			"id": 0,
			"parentId": 0,
			"studentName": "",
			"gender": 0,
			"grade": "",
			"schoolName": "",
			"weakSubjects": "",
			"studyDesc": "",
			"createTime": "",
			"updateTime": ""
		}
	],
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


# 教员模块


## 提交教员认证


**接口地址**:`/api/tutor/certification`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "realName": "",
  "idCard": "",
  "idCardFrontUrl": "",
  "idCardBackUrl": "",
  "universityName": "",
  "major": "",
  "education": 0,
  "enrollYear": 0,
  "studentCardUrl": "",
  "certificateUrls": []
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|tutorCertRequest|TutorCertRequest|body|true|TutorCertRequest|TutorCertRequest|
|&emsp;&emsp;realName|||true|string||
|&emsp;&emsp;idCard|||true|string||
|&emsp;&emsp;idCardFrontUrl|||true|string||
|&emsp;&emsp;idCardBackUrl|||true|string||
|&emsp;&emsp;universityName|||true|string||
|&emsp;&emsp;major|||true|string||
|&emsp;&emsp;education|||true|integer(int32)||
|&emsp;&emsp;enrollYear|||true|integer(int32)||
|&emsp;&emsp;studentCardUrl|||true|string||
|&emsp;&emsp;certificateUrls|||false|array|string|


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultVoid|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 获取当前教员档案


**接口地址**:`/api/tutor/profile`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


暂无


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultTutorProfile|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||TutorProfile|TutorProfile|
|&emsp;&emsp;id||integer(int64)||
|&emsp;&emsp;userId||integer(int64)||
|&emsp;&emsp;realName||string||
|&emsp;&emsp;idCard||string||
|&emsp;&emsp;idCardFrontUrl||string||
|&emsp;&emsp;idCardBackUrl||string||
|&emsp;&emsp;universityName||string||
|&emsp;&emsp;major||string||
|&emsp;&emsp;education||integer(int32)||
|&emsp;&emsp;enrollYear||integer(int32)||
|&emsp;&emsp;studentCardUrl||string||
|&emsp;&emsp;certificateUrls||string||
|&emsp;&emsp;teachSubjects||string||
|&emsp;&emsp;teachGrades||string||
|&emsp;&emsp;teachStyle||string||
|&emsp;&emsp;introduction||string||
|&emsp;&emsp;expectPrice||number||
|&emsp;&emsp;canVisit||integer(int32)||
|&emsp;&emsp;canOnline||integer(int32)||
|&emsp;&emsp;longitude||number||
|&emsp;&emsp;latitude||number||
|&emsp;&emsp;address||string||
|&emsp;&emsp;certStatus||integer(int32)||
|&emsp;&emsp;rejectReason||string||
|&emsp;&emsp;rating||number||
|&emsp;&emsp;orderCount||integer(int32)||
|&emsp;&emsp;createTime||string(date-time)||
|&emsp;&emsp;updateTime||string(date-time)||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"id": 0,
		"userId": 0,
		"realName": "",
		"idCard": "",
		"idCardFrontUrl": "",
		"idCardBackUrl": "",
		"universityName": "",
		"major": "",
		"education": 0,
		"enrollYear": 0,
		"studentCardUrl": "",
		"certificateUrls": "",
		"teachSubjects": "",
		"teachGrades": "",
		"teachStyle": "",
		"introduction": "",
		"expectPrice": 0,
		"canVisit": 0,
		"canOnline": 0,
		"longitude": 0,
		"latitude": 0,
		"address": "",
		"certStatus": 0,
		"rejectReason": "",
		"rating": 0,
		"orderCount": 0,
		"createTime": "",
		"updateTime": ""
	},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 更新教员档案


**接口地址**:`/api/tutor/profile`


**请求方式**:`PUT`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "teachSubjects": [],
  "teachGrades": [],
  "teachStyle": "",
  "introduction": "",
  "expectPrice": 0,
  "canVisit": 0,
  "canOnline": 0,
  "longitude": 0,
  "latitude": 0,
  "address": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|tutorProfileUpdateRequest|TutorProfileUpdateRequest|body|true|TutorProfileUpdateRequest|TutorProfileUpdateRequest|
|&emsp;&emsp;teachSubjects|||false|array|string|
|&emsp;&emsp;teachGrades|||false|array|string|
|&emsp;&emsp;teachStyle|||false|string||
|&emsp;&emsp;introduction|||false|string||
|&emsp;&emsp;expectPrice|||false|number||
|&emsp;&emsp;canVisit|||false|integer(int32)||
|&emsp;&emsp;canOnline|||false|integer(int32)||
|&emsp;&emsp;longitude|||false|number||
|&emsp;&emsp;latitude|||false|number||
|&emsp;&emsp;address|||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultVoid|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 根据ID获取教员档案(公开)


**接口地址**:`/api/tutor/public/{id}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id||path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultTutorProfile|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||TutorProfile|TutorProfile|
|&emsp;&emsp;id||integer(int64)||
|&emsp;&emsp;userId||integer(int64)||
|&emsp;&emsp;realName||string||
|&emsp;&emsp;idCard||string||
|&emsp;&emsp;idCardFrontUrl||string||
|&emsp;&emsp;idCardBackUrl||string||
|&emsp;&emsp;universityName||string||
|&emsp;&emsp;major||string||
|&emsp;&emsp;education||integer(int32)||
|&emsp;&emsp;enrollYear||integer(int32)||
|&emsp;&emsp;studentCardUrl||string||
|&emsp;&emsp;certificateUrls||string||
|&emsp;&emsp;teachSubjects||string||
|&emsp;&emsp;teachGrades||string||
|&emsp;&emsp;teachStyle||string||
|&emsp;&emsp;introduction||string||
|&emsp;&emsp;expectPrice||number||
|&emsp;&emsp;canVisit||integer(int32)||
|&emsp;&emsp;canOnline||integer(int32)||
|&emsp;&emsp;longitude||number||
|&emsp;&emsp;latitude||number||
|&emsp;&emsp;address||string||
|&emsp;&emsp;certStatus||integer(int32)||
|&emsp;&emsp;rejectReason||string||
|&emsp;&emsp;rating||number||
|&emsp;&emsp;orderCount||integer(int32)||
|&emsp;&emsp;createTime||string(date-time)||
|&emsp;&emsp;updateTime||string(date-time)||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"id": 0,
		"userId": 0,
		"realName": "",
		"idCard": "",
		"idCardFrontUrl": "",
		"idCardBackUrl": "",
		"universityName": "",
		"major": "",
		"education": 0,
		"enrollYear": 0,
		"studentCardUrl": "",
		"certificateUrls": "",
		"teachSubjects": "",
		"teachGrades": "",
		"teachStyle": "",
		"introduction": "",
		"expectPrice": 0,
		"canVisit": 0,
		"canOnline": 0,
		"longitude": 0,
		"latitude": 0,
		"address": "",
		"certStatus": 0,
		"rejectReason": "",
		"rating": 0,
		"orderCount": 0,
		"createTime": "",
		"updateTime": ""
	},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 获取时间配置


**接口地址**:`/api/tutor/schedule`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


暂无


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultObject|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 保存时间配置


**接口地址**:`/api/tutor/schedule`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "schedules": [
    {
      "dayOfWeek": 0,
      "startTime": "",
      "endTime": "",
      "available": 0
    }
  ]
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|tutorScheduleRequest|TutorScheduleRequest|body|true|TutorScheduleRequest|TutorScheduleRequest|
|&emsp;&emsp;schedules|||false|array|ScheduleItem|
|&emsp;&emsp;&emsp;&emsp;dayOfWeek|||false|integer||
|&emsp;&emsp;&emsp;&emsp;startTime|||false|string||
|&emsp;&emsp;&emsp;&emsp;endTime|||false|string||
|&emsp;&emsp;&emsp;&emsp;available|||false|integer||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultVoid|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


# 课时打卡


## 教师打卡上课


**接口地址**:`/api/teaching/check-in`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>教师开始上课时打卡，需要GPS定位和拍照</p>



**请求示例**:


```javascript
{
  "orderId": 0,
  "latitude": 0,
  "longitude": 0,
  "photoUrl": "",
  "contentSummary": "",
  "homeworkAssigned": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|checkInRequest|教师打卡请求|body|true|CheckInRequest|CheckInRequest|
|&emsp;&emsp;orderId|订单ID||true|integer(int64)||
|&emsp;&emsp;latitude|打卡纬度||true|number||
|&emsp;&emsp;longitude|打卡经度||true|number||
|&emsp;&emsp;photoUrl|现场拍照URL||true|string||
|&emsp;&emsp;contentSummary|教学内容摘要||false|string||
|&emsp;&emsp;homeworkAssigned|布置作业||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultLong|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||integer(int64)|integer(int64)|
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": 0,
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 教师打卡下课


**接口地址**:`/api/teaching/check-out/{recordId}`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>教师结束上课时打卡，可填写教学内容和作业</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|recordId|课时记录ID|path|true|integer(int64)||
|contentSummary|教学内容摘要|query|false|string||
|homeworkAssigned|布置作业|query|false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultVoid|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 家长确认课时


**接口地址**:`/api/teaching/confirm/{recordId}`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>家长确认教师完成了本节课</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|recordId|课时记录ID|path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultVoid|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 家长申诉课时


**接口地址**:`/api/teaching/dispute/{recordId}`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>家长对课时有异议时发起申诉</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|recordId|课时记录ID|path|true|integer(int64)||
|reason|申诉原因|query|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultVoid|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 获取我的课时记录


**接口地址**:`/api/teaching/my-records`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>根据用户角色获取相关的所有课时记录</p>



**请求参数**:


暂无


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultListTeachingRecordDTO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||array|TeachingRecordDTO|
|&emsp;&emsp;id|记录ID|integer(int64)||
|&emsp;&emsp;orderId|订单ID|integer(int64)||
|&emsp;&emsp;lessonIndex|第几节课|integer(int32)||
|&emsp;&emsp;startTime|上课时间|string(date-time)||
|&emsp;&emsp;endTime|下课时间|string(date-time)||
|&emsp;&emsp;clockInLat|打卡纬度|number||
|&emsp;&emsp;clockInLng|打卡经度|number||
|&emsp;&emsp;clockInImg|现场拍照|string||
|&emsp;&emsp;contentSummary|教学内容摘要|string||
|&emsp;&emsp;homeworkAssigned|布置作业|string||
|&emsp;&emsp;status|状态：0-待确认, 1-家长已确认, 2-异常/申诉|integer(int32)||
|&emsp;&emsp;statusText|状态文本|string||
|&emsp;&emsp;createTime|创建时间|string(date-time)||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": [
		{
			"id": 0,
			"orderId": 0,
			"lessonIndex": 0,
			"startTime": "",
			"endTime": "",
			"clockInLat": 0,
			"clockInLng": 0,
			"clockInImg": "",
			"contentSummary": "",
			"homeworkAssigned": "",
			"status": 0,
			"statusText": "",
			"createTime": ""
		}
	],
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 获取课时记录详情


**接口地址**:`/api/teaching/record/{recordId}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>获取单条课时记录详情</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|recordId|课时记录ID|path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultTeachingRecordDTO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||TeachingRecordDTO|TeachingRecordDTO|
|&emsp;&emsp;id|记录ID|integer(int64)||
|&emsp;&emsp;orderId|订单ID|integer(int64)||
|&emsp;&emsp;lessonIndex|第几节课|integer(int32)||
|&emsp;&emsp;startTime|上课时间|string(date-time)||
|&emsp;&emsp;endTime|下课时间|string(date-time)||
|&emsp;&emsp;clockInLat|打卡纬度|number||
|&emsp;&emsp;clockInLng|打卡经度|number||
|&emsp;&emsp;clockInImg|现场拍照|string||
|&emsp;&emsp;contentSummary|教学内容摘要|string||
|&emsp;&emsp;homeworkAssigned|布置作业|string||
|&emsp;&emsp;status|状态：0-待确认, 1-家长已确认, 2-异常/申诉|integer(int32)||
|&emsp;&emsp;statusText|状态文本|string||
|&emsp;&emsp;createTime|创建时间|string(date-time)||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"id": 0,
		"orderId": 0,
		"lessonIndex": 0,
		"startTime": "",
		"endTime": "",
		"clockInLat": 0,
		"clockInLng": 0,
		"clockInImg": "",
		"contentSummary": "",
		"homeworkAssigned": "",
		"status": 0,
		"statusText": "",
		"createTime": ""
	},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 获取订单课时记录


**接口地址**:`/api/teaching/records/{orderId}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>获取指定订单的所有课时记录</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|orderId|订单ID|path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultListTeachingRecordDTO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||array|TeachingRecordDTO|
|&emsp;&emsp;id|记录ID|integer(int64)||
|&emsp;&emsp;orderId|订单ID|integer(int64)||
|&emsp;&emsp;lessonIndex|第几节课|integer(int32)||
|&emsp;&emsp;startTime|上课时间|string(date-time)||
|&emsp;&emsp;endTime|下课时间|string(date-time)||
|&emsp;&emsp;clockInLat|打卡纬度|number||
|&emsp;&emsp;clockInLng|打卡经度|number||
|&emsp;&emsp;clockInImg|现场拍照|string||
|&emsp;&emsp;contentSummary|教学内容摘要|string||
|&emsp;&emsp;homeworkAssigned|布置作业|string||
|&emsp;&emsp;status|状态：0-待确认, 1-家长已确认, 2-异常/申诉|integer(int32)||
|&emsp;&emsp;statusText|状态文本|string||
|&emsp;&emsp;createTime|创建时间|string(date-time)||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": [
		{
			"id": 0,
			"orderId": 0,
			"lessonIndex": 0,
			"startTime": "",
			"endTime": "",
			"clockInLat": 0,
			"clockInLng": 0,
			"clockInImg": "",
			"contentSummary": "",
			"homeworkAssigned": "",
			"status": 0,
			"statusText": "",
			"createTime": ""
		}
	],
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


# 匹配模块


## 搜索教员(GET简化版)


**接口地址**:`/api/match/tutors`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|subject||query|false|string||
|grade||query|false|string||
|longitude||query|false|number(double)||
|latitude||query|false|number(double)||
|radius||query|false|number(double)||
|page||query|false|integer(int32)||
|size||query|false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultIPageTutorSearchResult|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||IPageTutorSearchResult|IPageTutorSearchResult|
|&emsp;&emsp;size||integer(int64)||
|&emsp;&emsp;total||integer(int64)||
|&emsp;&emsp;records||array|TutorSearchResult|
|&emsp;&emsp;&emsp;&emsp;id||integer||
|&emsp;&emsp;&emsp;&emsp;userId||integer||
|&emsp;&emsp;&emsp;&emsp;realName||string||
|&emsp;&emsp;&emsp;&emsp;avatarUrl||string||
|&emsp;&emsp;&emsp;&emsp;universityName||string||
|&emsp;&emsp;&emsp;&emsp;major||string||
|&emsp;&emsp;&emsp;&emsp;education||integer||
|&emsp;&emsp;&emsp;&emsp;teachSubjects||array|string|
|&emsp;&emsp;&emsp;&emsp;teachGrades||array|string|
|&emsp;&emsp;&emsp;&emsp;teachStyle||string||
|&emsp;&emsp;&emsp;&emsp;introduction||string||
|&emsp;&emsp;&emsp;&emsp;expectPrice||number||
|&emsp;&emsp;&emsp;&emsp;canVisit||integer||
|&emsp;&emsp;&emsp;&emsp;canOnline||integer||
|&emsp;&emsp;&emsp;&emsp;rating||number||
|&emsp;&emsp;&emsp;&emsp;orderCount||integer||
|&emsp;&emsp;&emsp;&emsp;distance||number||
|&emsp;&emsp;current||integer(int64)||
|&emsp;&emsp;pages||integer(int64)||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"size": 0,
		"total": 0,
		"records": [
			{
				"id": 0,
				"userId": 0,
				"realName": "",
				"avatarUrl": "",
				"universityName": "",
				"major": "",
				"education": 0,
				"teachSubjects": [],
				"teachGrades": [],
				"teachStyle": "",
				"introduction": "",
				"expectPrice": 0,
				"canVisit": 0,
				"canOnline": 0,
				"rating": 0,
				"orderCount": 0,
				"distance": 0
			}
		],
		"current": 0,
		"pages": 0
	},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 搜索教员


**接口地址**:`/api/match/tutors`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "subject": "",
  "grade": "",
  "minPrice": 0,
  "maxPrice": 0,
  "teachMode": 0,
  "educations": [],
  "gender": 0,
  "longitude": 0,
  "latitude": 0,
  "radius": 0,
  "sortBy": "",
  "sortOrder": "",
  "page": 0,
  "size": 0
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|tutorSearchRequest|TutorSearchRequest|body|true|TutorSearchRequest|TutorSearchRequest|
|&emsp;&emsp;subject|||false|string||
|&emsp;&emsp;grade|||false|string||
|&emsp;&emsp;minPrice|||false|number||
|&emsp;&emsp;maxPrice|||false|number||
|&emsp;&emsp;teachMode|||false|integer(int32)||
|&emsp;&emsp;educations|||false|array|integer(int32)|
|&emsp;&emsp;gender|||false|integer(int32)||
|&emsp;&emsp;longitude|||false|number(double)||
|&emsp;&emsp;latitude|||false|number(double)||
|&emsp;&emsp;radius|||false|number(double)||
|&emsp;&emsp;sortBy|||false|string||
|&emsp;&emsp;sortOrder|||false|string||
|&emsp;&emsp;page|||false|integer(int32)||
|&emsp;&emsp;size|||false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultIPageTutorSearchResult|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||IPageTutorSearchResult|IPageTutorSearchResult|
|&emsp;&emsp;size||integer(int64)||
|&emsp;&emsp;total||integer(int64)||
|&emsp;&emsp;records||array|TutorSearchResult|
|&emsp;&emsp;&emsp;&emsp;id||integer||
|&emsp;&emsp;&emsp;&emsp;userId||integer||
|&emsp;&emsp;&emsp;&emsp;realName||string||
|&emsp;&emsp;&emsp;&emsp;avatarUrl||string||
|&emsp;&emsp;&emsp;&emsp;universityName||string||
|&emsp;&emsp;&emsp;&emsp;major||string||
|&emsp;&emsp;&emsp;&emsp;education||integer||
|&emsp;&emsp;&emsp;&emsp;teachSubjects||array|string|
|&emsp;&emsp;&emsp;&emsp;teachGrades||array|string|
|&emsp;&emsp;&emsp;&emsp;teachStyle||string||
|&emsp;&emsp;&emsp;&emsp;introduction||string||
|&emsp;&emsp;&emsp;&emsp;expectPrice||number||
|&emsp;&emsp;&emsp;&emsp;canVisit||integer||
|&emsp;&emsp;&emsp;&emsp;canOnline||integer||
|&emsp;&emsp;&emsp;&emsp;rating||number||
|&emsp;&emsp;&emsp;&emsp;orderCount||integer||
|&emsp;&emsp;&emsp;&emsp;distance||number||
|&emsp;&emsp;current||integer(int64)||
|&emsp;&emsp;pages||integer(int64)||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"size": 0,
		"total": 0,
		"records": [
			{
				"id": 0,
				"userId": 0,
				"realName": "",
				"avatarUrl": "",
				"universityName": "",
				"major": "",
				"education": 0,
				"teachSubjects": [],
				"teachGrades": [],
				"teachStyle": "",
				"introduction": "",
				"expectPrice": 0,
				"canVisit": 0,
				"canOnline": 0,
				"rating": 0,
				"orderCount": 0,
				"distance": 0
			}
		],
		"current": 0,
		"pages": 0
	},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


# 钱包模块


## 获取当前用户钱包信息


**接口地址**:`/api/wallet`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


暂无


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultSysWallet|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||SysWallet|SysWallet|
|&emsp;&emsp;userId||integer(int64)||
|&emsp;&emsp;balance||number||
|&emsp;&emsp;frozenAmount||number||
|&emsp;&emsp;payPassword||string||
|&emsp;&emsp;version||integer(int32)||
|&emsp;&emsp;updateTime||string(date-time)||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"userId": 0,
		"balance": 0,
		"frozenAmount": 0,
		"payPassword": "",
		"version": 0,
		"updateTime": ""
	},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 分页获取交易流水


**接口地址**:`/api/wallet/transactions`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|page|页码|query|false|integer(int32)||
|size|每页大小|query|false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultIPageSysTransactionFlow|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||IPageSysTransactionFlow|IPageSysTransactionFlow|
|&emsp;&emsp;size||integer(int64)||
|&emsp;&emsp;total||integer(int64)||
|&emsp;&emsp;records||array|SysTransactionFlow|
|&emsp;&emsp;&emsp;&emsp;id||integer||
|&emsp;&emsp;&emsp;&emsp;userId||integer||
|&emsp;&emsp;&emsp;&emsp;amount||number||
|&emsp;&emsp;&emsp;&emsp;balanceAfter||number||
|&emsp;&emsp;&emsp;&emsp;flowType||integer||
|&emsp;&emsp;&emsp;&emsp;orderId||integer||
|&emsp;&emsp;&emsp;&emsp;remark||string||
|&emsp;&emsp;&emsp;&emsp;createTime||string||
|&emsp;&emsp;current||integer(int64)||
|&emsp;&emsp;pages||integer(int64)||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"size": 0,
		"total": 0,
		"records": [
			{
				"id": 0,
				"userId": 0,
				"amount": 0,
				"balanceAfter": 0,
				"flowType": 0,
				"orderId": 0,
				"remark": "",
				"createTime": ""
			}
		],
		"current": 0,
		"pages": 0
	},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 发起提现申请


**接口地址**:`/api/wallet/withdraw`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "amount": 100,
  "channel": 1,
  "accountNo": "example@alipay.com",
  "payPassword": "123456"
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|withdrawRequest|提现请求|body|true|WithdrawRequest|WithdrawRequest|
|&emsp;&emsp;amount|提现金额||true|number||
|&emsp;&emsp;channel|渠道: 1-微信, 2-支付宝, 3-银行卡||true|integer(int32)||
|&emsp;&emsp;accountNo|收款账号||true|string||
|&emsp;&emsp;payPassword|支付密码||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultLong|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||integer(int64)|integer(int64)|
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": 0,
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 获取提现记录列表


**接口地址**:`/api/wallet/withdrawals`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|page|页码|query|false|integer(int32)||
|size|每页大小|query|false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultIPageSysWithdrawal|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||IPageSysWithdrawal|IPageSysWithdrawal|
|&emsp;&emsp;size||integer(int64)||
|&emsp;&emsp;total||integer(int64)||
|&emsp;&emsp;records||array|SysWithdrawal|
|&emsp;&emsp;&emsp;&emsp;id||integer||
|&emsp;&emsp;&emsp;&emsp;userId||integer||
|&emsp;&emsp;&emsp;&emsp;amount||number||
|&emsp;&emsp;&emsp;&emsp;channel||integer||
|&emsp;&emsp;&emsp;&emsp;accountNo||string||
|&emsp;&emsp;&emsp;&emsp;status||integer||
|&emsp;&emsp;&emsp;&emsp;auditRemark||string||
|&emsp;&emsp;&emsp;&emsp;createTime||string||
|&emsp;&emsp;current||integer(int64)||
|&emsp;&emsp;pages||integer(int64)||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"size": 0,
		"total": 0,
		"records": [
			{
				"id": 0,
				"userId": 0,
				"amount": 0,
				"channel": 0,
				"accountNo": "",
				"status": 0,
				"auditRemark": "",
				"createTime": ""
			}
		],
		"current": 0,
		"pages": 0
	},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


# 认证管理


## 用户登录


**接口地址**:`/api/auth/login`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>支持密码登录和验证码登录</p>



**请求示例**:


```javascript
{
  "account": "13800138000 或 admin01",
  "password": "",
  "code": "123456",
  "loginType": "password"
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|loginRequest|登录请求|body|true|LoginRequest|LoginRequest|
|&emsp;&emsp;account|账号/手机号||true|string||
|&emsp;&emsp;password|密码 (与验证码二选一)||false|string||
|&emsp;&emsp;code|验证码 (与密码二选一)||false|string||
|&emsp;&emsp;loginType|登录方式: password-密码登录, code-验证码登录||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultLoginResponse|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||LoginResponse|LoginResponse|
|&emsp;&emsp;token|JWT Token|string||
|&emsp;&emsp;userId|用户ID|integer(int64)||
|&emsp;&emsp;username|用户名|string||
|&emsp;&emsp;nickname|昵称|string||
|&emsp;&emsp;avatar|头像|string||
|&emsp;&emsp;role|角色: 0-管理员, 1-教员, 2-家长|integer(int32)||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"token": "",
		"userId": 0,
		"username": "",
		"nickname": "",
		"avatar": "",
		"role": 0
	},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 用户注册


**接口地址**:`/api/auth/register`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>注册新用户，需要验证码</p>



**请求示例**:


```javascript
{
  "phone": "13800138000",
  "password": "123456",
  "code": "123456",
  "nickname": "",
  "role": 2
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|registerRequest|注册请求|body|true|RegisterRequest|RegisterRequest|
|&emsp;&emsp;phone|手机号||true|string||
|&emsp;&emsp;password|密码||true|string||
|&emsp;&emsp;code|验证码||true|string||
|&emsp;&emsp;nickname|昵称||false|string||
|&emsp;&emsp;role|角色: 1-教员, 2-家长||true|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultLoginResponse|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||LoginResponse|LoginResponse|
|&emsp;&emsp;token|JWT Token|string||
|&emsp;&emsp;userId|用户ID|integer(int64)||
|&emsp;&emsp;username|用户名|string||
|&emsp;&emsp;nickname|昵称|string||
|&emsp;&emsp;avatar|头像|string||
|&emsp;&emsp;role|角色: 0-管理员, 1-教员, 2-家长|integer(int32)||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"token": "",
		"userId": 0,
		"username": "",
		"nickname": "",
		"avatar": "",
		"role": 0
	},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 发送验证码


**接口地址**:`/api/auth/send-code`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>发送短信验证码 (Mock)</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|phone|手机号|query|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultVoid|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


# 文件管理


## 删除文件


**接口地址**:`/api/file`


**请求方式**:`DELETE`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|fileUrl|文件URL|query|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultBoolean|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||boolean||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": true,
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 上传文件


**接口地址**:`/api/file/upload`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>上传图片或PDF文件，返回访问URL</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|file|文件|query|true|file||
|folder|目录: avatar/cert/clock-in 等|query|false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultString|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||string||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": "",
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


# 需求模块


## 需求详情


**接口地址**:`/api/demand/{id}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id||path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultDemandPost|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||DemandPost|DemandPost|
|&emsp;&emsp;id||integer(int64)||
|&emsp;&emsp;publisherId||integer(int64)||
|&emsp;&emsp;studentId||integer(int64)||
|&emsp;&emsp;title||string||
|&emsp;&emsp;subject||string||
|&emsp;&emsp;grade||string||
|&emsp;&emsp;expectPrice||number||
|&emsp;&emsp;scheduleRequire||string||
|&emsp;&emsp;teachMode||integer(int32)||
|&emsp;&emsp;longitude||number||
|&emsp;&emsp;latitude||number||
|&emsp;&emsp;address||string||
|&emsp;&emsp;detail||string||
|&emsp;&emsp;status||integer(int32)||
|&emsp;&emsp;matchedTutorId||integer(int64)||
|&emsp;&emsp;createTime||string(date-time)||
|&emsp;&emsp;updateTime||string(date-time)||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"id": 0,
		"publisherId": 0,
		"studentId": 0,
		"title": "",
		"subject": "",
		"grade": "",
		"expectPrice": 0,
		"scheduleRequire": "",
		"teachMode": 0,
		"longitude": 0,
		"latitude": 0,
		"address": "",
		"detail": "",
		"status": 0,
		"matchedTutorId": 0,
		"createTime": "",
		"updateTime": ""
	},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 删除需求


**接口地址**:`/api/demand/{id}`


**请求方式**:`DELETE`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id||path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultVoid|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 下架需求


**接口地址**:`/api/demand/{id}/offline`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id||path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultVoid|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 上架需求


**接口地址**:`/api/demand/{id}/online`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id||path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultVoid|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 分页查询需求列表(公开)


**接口地址**:`/api/demand/list`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|subject||query|false|string||
|grade||query|false|string||
|page||query|false|integer(int32)||
|size||query|false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultIPageDemandPost|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||IPageDemandPost|IPageDemandPost|
|&emsp;&emsp;size||integer(int64)||
|&emsp;&emsp;total||integer(int64)||
|&emsp;&emsp;records||array|DemandPost|
|&emsp;&emsp;&emsp;&emsp;id||integer||
|&emsp;&emsp;&emsp;&emsp;publisherId||integer||
|&emsp;&emsp;&emsp;&emsp;studentId||integer||
|&emsp;&emsp;&emsp;&emsp;title||string||
|&emsp;&emsp;&emsp;&emsp;subject||string||
|&emsp;&emsp;&emsp;&emsp;grade||string||
|&emsp;&emsp;&emsp;&emsp;expectPrice||number||
|&emsp;&emsp;&emsp;&emsp;scheduleRequire||string||
|&emsp;&emsp;&emsp;&emsp;teachMode||integer||
|&emsp;&emsp;&emsp;&emsp;longitude||number||
|&emsp;&emsp;&emsp;&emsp;latitude||number||
|&emsp;&emsp;&emsp;&emsp;address||string||
|&emsp;&emsp;&emsp;&emsp;detail||string||
|&emsp;&emsp;&emsp;&emsp;status||integer||
|&emsp;&emsp;&emsp;&emsp;matchedTutorId||integer||
|&emsp;&emsp;&emsp;&emsp;createTime||string||
|&emsp;&emsp;&emsp;&emsp;updateTime||string||
|&emsp;&emsp;current||integer(int64)||
|&emsp;&emsp;pages||integer(int64)||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"size": 0,
		"total": 0,
		"records": [
			{
				"id": 0,
				"publisherId": 0,
				"studentId": 0,
				"title": "",
				"subject": "",
				"grade": "",
				"expectPrice": 0,
				"scheduleRequire": "",
				"teachMode": 0,
				"longitude": 0,
				"latitude": 0,
				"address": "",
				"detail": "",
				"status": 0,
				"matchedTutorId": 0,
				"createTime": "",
				"updateTime": ""
			}
		],
		"current": 0,
		"pages": 0
	},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 我发布的需求列表


**接口地址**:`/api/demand/my`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


暂无


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultListDemandPost|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||array|DemandPost|
|&emsp;&emsp;id||integer(int64)||
|&emsp;&emsp;publisherId||integer(int64)||
|&emsp;&emsp;studentId||integer(int64)||
|&emsp;&emsp;title||string||
|&emsp;&emsp;subject||string||
|&emsp;&emsp;grade||string||
|&emsp;&emsp;expectPrice||number||
|&emsp;&emsp;scheduleRequire||string||
|&emsp;&emsp;teachMode||integer(int32)||
|&emsp;&emsp;longitude||number||
|&emsp;&emsp;latitude||number||
|&emsp;&emsp;address||string||
|&emsp;&emsp;detail||string||
|&emsp;&emsp;status||integer(int32)||
|&emsp;&emsp;matchedTutorId||integer(int64)||
|&emsp;&emsp;createTime||string(date-time)||
|&emsp;&emsp;updateTime||string(date-time)||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": [
		{
			"id": 0,
			"publisherId": 0,
			"studentId": 0,
			"title": "",
			"subject": "",
			"grade": "",
			"expectPrice": 0,
			"scheduleRequire": "",
			"teachMode": 0,
			"longitude": 0,
			"latitude": 0,
			"address": "",
			"detail": "",
			"status": 0,
			"matchedTutorId": 0,
			"createTime": "",
			"updateTime": ""
		}
	],
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 附近需求搜索(LBS)


**接口地址**:`/api/demand/nearby`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|longitude||query|true|number(double)||
|latitude||query|true|number(double)||
|radius||query|false|number(double)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultListDemandPost|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||array|DemandPost|
|&emsp;&emsp;id||integer(int64)||
|&emsp;&emsp;publisherId||integer(int64)||
|&emsp;&emsp;studentId||integer(int64)||
|&emsp;&emsp;title||string||
|&emsp;&emsp;subject||string||
|&emsp;&emsp;grade||string||
|&emsp;&emsp;expectPrice||number||
|&emsp;&emsp;scheduleRequire||string||
|&emsp;&emsp;teachMode||integer(int32)||
|&emsp;&emsp;longitude||number||
|&emsp;&emsp;latitude||number||
|&emsp;&emsp;address||string||
|&emsp;&emsp;detail||string||
|&emsp;&emsp;status||integer(int32)||
|&emsp;&emsp;matchedTutorId||integer(int64)||
|&emsp;&emsp;createTime||string(date-time)||
|&emsp;&emsp;updateTime||string(date-time)||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": [
		{
			"id": 0,
			"publisherId": 0,
			"studentId": 0,
			"title": "",
			"subject": "",
			"grade": "",
			"expectPrice": 0,
			"scheduleRequire": "",
			"teachMode": 0,
			"longitude": 0,
			"latitude": 0,
			"address": "",
			"detail": "",
			"status": 0,
			"matchedTutorId": 0,
			"createTime": "",
			"updateTime": ""
		}
	],
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 发布需求


**接口地址**:`/api/demand/publish`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "id": 0,
  "studentId": 0,
  "title": "",
  "subject": "",
  "grade": "",
  "expectPrice": 0,
  "scheduleRequire": [],
  "teachMode": 0,
  "longitude": 0,
  "latitude": 0,
  "address": "",
  "detail": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|demandPostRequest|DemandPostRequest|body|true|DemandPostRequest|DemandPostRequest|
|&emsp;&emsp;id|||false|integer(int64)||
|&emsp;&emsp;studentId|||false|integer(int64)||
|&emsp;&emsp;title|||true|string||
|&emsp;&emsp;subject|||true|string||
|&emsp;&emsp;grade|||true|string||
|&emsp;&emsp;expectPrice|||false|number||
|&emsp;&emsp;scheduleRequire|||false|array|string|
|&emsp;&emsp;teachMode|||true|integer(int32)||
|&emsp;&emsp;longitude|||false|number||
|&emsp;&emsp;latitude|||false|number||
|&emsp;&emsp;address|||false|string||
|&emsp;&emsp;detail|||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultLong|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||integer(int64)|integer(int64)|
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": 0,
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 更新需求


**接口地址**:`/api/demand/update`


**请求方式**:`PUT`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "id": 0,
  "studentId": 0,
  "title": "",
  "subject": "",
  "grade": "",
  "expectPrice": 0,
  "scheduleRequire": [],
  "teachMode": 0,
  "longitude": 0,
  "latitude": 0,
  "address": "",
  "detail": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|demandPostRequest|DemandPostRequest|body|true|DemandPostRequest|DemandPostRequest|
|&emsp;&emsp;id|||false|integer(int64)||
|&emsp;&emsp;studentId|||false|integer(int64)||
|&emsp;&emsp;title|||true|string||
|&emsp;&emsp;subject|||true|string||
|&emsp;&emsp;grade|||true|string||
|&emsp;&emsp;expectPrice|||false|number||
|&emsp;&emsp;scheduleRequire|||false|array|string|
|&emsp;&emsp;teachMode|||true|integer(int32)||
|&emsp;&emsp;longitude|||false|number||
|&emsp;&emsp;latitude|||false|number||
|&emsp;&emsp;address|||false|string||
|&emsp;&emsp;detail|||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultVoid|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


# 用户管理


## 更新用户信息


**接口地址**:`/api/user`


**请求方式**:`PUT`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "id": 0,
  "username": "",
  "password": "",
  "nickname": "",
  "avatarUrl": "",
  "role": 0,
  "openid": "",
  "status": 0,
  "gender": 0,
  "createTime": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|sysUser|SysUser|body|true|SysUser|SysUser|
|&emsp;&emsp;id|||false|integer(int64)||
|&emsp;&emsp;username|||false|string||
|&emsp;&emsp;password|||false|string||
|&emsp;&emsp;nickname|||false|string||
|&emsp;&emsp;avatarUrl|||false|string||
|&emsp;&emsp;role|||false|integer(int32)||
|&emsp;&emsp;openid|||false|string||
|&emsp;&emsp;status|||false|integer(int32)||
|&emsp;&emsp;gender|||false|integer(int32)||
|&emsp;&emsp;createTime|||false|string(date-time)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultBoolean|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||boolean||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": true,
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 根据ID获取用户信息


**接口地址**:`/api/user/{id}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|用户ID|path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultSysUser|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||SysUser|SysUser|
|&emsp;&emsp;id||integer(int64)||
|&emsp;&emsp;username||string||
|&emsp;&emsp;password||string||
|&emsp;&emsp;nickname||string||
|&emsp;&emsp;avatarUrl||string||
|&emsp;&emsp;role||integer(int32)||
|&emsp;&emsp;openid||string||
|&emsp;&emsp;status||integer(int32)||
|&emsp;&emsp;gender||integer(int32)||
|&emsp;&emsp;createTime||string(date-time)||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"id": 0,
		"username": "",
		"password": "",
		"nickname": "",
		"avatarUrl": "",
		"role": 0,
		"openid": "",
		"status": 0,
		"gender": 0,
		"createTime": ""
	},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 更新用户状态


**接口地址**:`/api/user/{id}/status`


**请求方式**:`PUT`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|用户ID|path|true|integer(int64)||
|status|状态: 1-正常, 0-禁用|query|true|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultBoolean|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||boolean||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": true,
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 根据用户名获取用户信息


**接口地址**:`/api/user/username/{username}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|username|用户名|path|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultSysUser|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||SysUser|SysUser|
|&emsp;&emsp;id||integer(int64)||
|&emsp;&emsp;username||string||
|&emsp;&emsp;password||string||
|&emsp;&emsp;nickname||string||
|&emsp;&emsp;avatarUrl||string||
|&emsp;&emsp;role||integer(int32)||
|&emsp;&emsp;openid||string||
|&emsp;&emsp;status||integer(int32)||
|&emsp;&emsp;gender||integer(int32)||
|&emsp;&emsp;createTime||string(date-time)||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"id": 0,
		"username": "",
		"password": "",
		"nickname": "",
		"avatarUrl": "",
		"role": 0,
		"openid": "",
		"status": 0,
		"gender": 0,
		"createTime": ""
	},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


# 智能服务


## 智能对话


**接口地址**:`/api/llm/chat`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>与AI助手进行对话，获取家教相关帮助</p>



**请求示例**:


```javascript
{
  "messages": [
    {
      "role": "",
      "content": ""
    }
  ],
  "scene": "demand",
  "stream": false
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|chatRequest|智能对话请求|body|true|ChatRequest|ChatRequest|
|&emsp;&emsp;messages|对话消息列表||true|array|ChatMessage|
|&emsp;&emsp;&emsp;&emsp;role|角色: system, user, assistant||false|string||
|&emsp;&emsp;&emsp;&emsp;content|消息内容||false|string||
|&emsp;&emsp;scene|对话场景: demand-需求咨询, tutor-教员推荐, general-通用问答||false|string||
|&emsp;&emsp;stream|是否流式返回||false|boolean||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultChatResponse|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||ChatResponse|ChatResponse|
|&emsp;&emsp;success|是否成功|boolean||
|&emsp;&emsp;content|回复内容|string||
|&emsp;&emsp;tokensUsed|消耗的Token数|integer(int32)||
|&emsp;&emsp;error|错误信息|string||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"success": true,
		"content": "",
		"tokensUsed": 0,
		"error": ""
	},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 智能解析需求


**接口地址**:`/api/llm/demand/parse`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>使用AI从自然语言描述中提取结构化的家教需求信息</p>



**请求示例**:


```javascript
{
  "text": "孩子初二数学不太好，想找个有经验的女老师，最好是985本科以上，能上门辅导，预算150左右每小时，周末上午有时间",
  "debug": false
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|demandParseRequest|需求解析请求|body|true|DemandParseRequest|DemandParseRequest|
|&emsp;&emsp;text|用户输入的自然语言描述||true|string||
|&emsp;&emsp;debug|是否返回原始LLM响应(调试用)||false|boolean||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultDemandParseResult|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||DemandParseResult|DemandParseResult|
|&emsp;&emsp;success|解析是否成功|boolean||
|&emsp;&emsp;originalText|原始输入文本|string||
|&emsp;&emsp;subject|科目|string||
|&emsp;&emsp;grade|年级|string||
|&emsp;&emsp;expectPrice|预算价格|number||
|&emsp;&emsp;teachMode|授课方式: 1上门 2网课 3均可|integer(int32)||
|&emsp;&emsp;preferGender|教员性别偏好: 1男 2女|integer(int32)||
|&emsp;&emsp;educations|学历要求列表|array|integer(int32)|
|&emsp;&emsp;scheduleRequire|时间要求|string||
|&emsp;&emsp;address|地址信息|string||
|&emsp;&emsp;detail|需求详情|string||
|&emsp;&emsp;confidence|解析置信度|number(double)||
|&emsp;&emsp;suggestion|补充说明|string||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"success": true,
		"originalText": "",
		"subject": "数学",
		"grade": "初二",
		"expectPrice": 150,
		"teachMode": 1,
		"preferGender": 2,
		"educations": [],
		"scheduleRequire": "周末上午",
		"address": "北京市海淀区中关村",
		"detail": "",
		"confidence": 0.85,
		"suggestion": ""
	},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 快速问答


**接口地址**:`/api/llm/quick-answer`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>快速获取问题答案，无需上下文</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|question||query|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultString|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||string||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": "",
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


# OCR识别


## 通用文字识别


**接口地址**:`/api/ocr/general`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>识别图片中的文字内容</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|imageUrl|图片URL|query|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultString|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||string||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": "",
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 识别身份证背面


**接口地址**:`/api/ocr/id-card/back`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>识别身份证背面，返回签发机关等信息</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|imageUrl|身份证背面图片URL|query|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultOcrResultDTO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||OcrResultDTO|OcrResultDTO|
|&emsp;&emsp;success|识别是否成功|boolean||
|&emsp;&emsp;errorMsg|错误信息|string||
|&emsp;&emsp;realName|真实姓名|string||
|&emsp;&emsp;universityName|学校名称|string||
|&emsp;&emsp;major|专业|string||
|&emsp;&emsp;studentId|学号|string||
|&emsp;&emsp;enrollYear|入学年份|integer(int32)||
|&emsp;&emsp;idCard|身份证号|string||
|&emsp;&emsp;gender|性别|string||
|&emsp;&emsp;nation|民族|string||
|&emsp;&emsp;birthDate|出生日期|string||
|&emsp;&emsp;address|地址|string||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"success": true,
		"errorMsg": "",
		"realName": "",
		"universityName": "",
		"major": "",
		"studentId": "",
		"enrollYear": 0,
		"idCard": "",
		"gender": "",
		"nation": "",
		"birthDate": "",
		"address": ""
	},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 识别身份证正面


**接口地址**:`/api/ocr/id-card/front`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>识别身份证正面，返回姓名、身份证号等信息</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|imageUrl|身份证正面图片URL|query|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultOcrResultDTO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||OcrResultDTO|OcrResultDTO|
|&emsp;&emsp;success|识别是否成功|boolean||
|&emsp;&emsp;errorMsg|错误信息|string||
|&emsp;&emsp;realName|真实姓名|string||
|&emsp;&emsp;universityName|学校名称|string||
|&emsp;&emsp;major|专业|string||
|&emsp;&emsp;studentId|学号|string||
|&emsp;&emsp;enrollYear|入学年份|integer(int32)||
|&emsp;&emsp;idCard|身份证号|string||
|&emsp;&emsp;gender|性别|string||
|&emsp;&emsp;nation|民族|string||
|&emsp;&emsp;birthDate|出生日期|string||
|&emsp;&emsp;address|地址|string||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"success": true,
		"errorMsg": "",
		"realName": "",
		"universityName": "",
		"major": "",
		"studentId": "",
		"enrollYear": 0,
		"idCard": "",
		"gender": "",
		"nation": "",
		"birthDate": "",
		"address": ""
	},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


## 识别学生证


**接口地址**:`/api/ocr/student-card`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:<p>识别学生证图片，返回姓名、学校、专业等信息</p>



**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|imageUrl|学生证图片URL|query|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ResultOcrResultDTO|
|400|Bad Request|ResultVoid|
|404|Not Found|ResultVoid|
|405|Method Not Allowed|ResultVoid|
|500|Internal Server Error|ResultVoid|


**响应状态码-200**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||OcrResultDTO|OcrResultDTO|
|&emsp;&emsp;success|识别是否成功|boolean||
|&emsp;&emsp;errorMsg|错误信息|string||
|&emsp;&emsp;realName|真实姓名|string||
|&emsp;&emsp;universityName|学校名称|string||
|&emsp;&emsp;major|专业|string||
|&emsp;&emsp;studentId|学号|string||
|&emsp;&emsp;enrollYear|入学年份|integer(int32)||
|&emsp;&emsp;idCard|身份证号|string||
|&emsp;&emsp;gender|性别|string||
|&emsp;&emsp;nation|民族|string||
|&emsp;&emsp;birthDate|出生日期|string||
|&emsp;&emsp;address|地址|string||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {
		"success": true,
		"errorMsg": "",
		"realName": "",
		"universityName": "",
		"major": "",
		"studentId": "",
		"enrollYear": 0,
		"idCard": "",
		"gender": "",
		"nation": "",
		"birthDate": "",
		"address": ""
	},
	"timestamp": 0
}
```


**响应状态码-400**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-404**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-405**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```


**响应状态码-500**:


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|msg||string||
|data||object||
|timestamp||integer(int64)|integer(int64)|


**响应示例**:
```javascript
{
	"code": 0,
	"msg": "",
	"data": {},
	"timestamp": 0
}
```