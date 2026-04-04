package com.netsservices.dct.data.remote.response

import com.netsservices.dct.domain.model.Orchard
import com.netsservices.dct.domain.model.Plantation

data class ContractResponse(
    val items: List<ContractItem>,
    val total: Int
)

data class ContractItem(
    val id: String,
    val contractCode: String,
    val sellerId: String,
    val seller: UserInfo,
    val operatorUserId: String,
    val operatorUser: UserInfo,
    val plantationId: String,
    val orchardId: String,
    val siteId: String,
    val plantation: Plantation,
    val orchard: Orchard,
    val site: Site,
    val tier: String,
    val startDate: String,
    val endDate: String,
    val autoRenew: Boolean,
    val monthlyFee: Double,
    val currency: String,
    val scanLimitPerDay: Int,
    val sensorModules: List<String>,
    val status: String,
    val approvedBy: UserInfo?,
    val approvedAt: String?,
    val suspendedAt: String?,
    val cancelledAt: String?,
    val notes: String?,
    val createdAt: String,
    val updatedAt: String
)

data class UserInfo(
    val id: String,
    val email: String
)
