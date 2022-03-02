package com.sms.moLotus.feature.model

data class APNParamDetails(
    val id: Int,
    val operator:Int,
    val apn_name: String,
    val apn: String,
    val proxy: String,
    val port: String,
    val username: String,
    val password: String,
    val server: String,
    val mmsc: String,
    val mms_proxy: String,
    val mms_port: String,
    val mcc: String,
    val mnc: String,
    val auth_type: String,
    val apn_type: String,
    val apn_protocol: String,
    val apn_roaming: String,
    val bearer: String,
    val mvno_type: String,
    val mvno_value: String,
    val deleted_at: String,
    val created_at: String,
    val updated_at: String,
)