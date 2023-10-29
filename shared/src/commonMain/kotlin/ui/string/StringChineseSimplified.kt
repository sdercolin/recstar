package ui.string

import ui.string.Strings.*

@Suppress("REDUNDANT_ELSE_IN_WHEN")
fun Strings.zhHans(): String? =
    when (this) {
        CommonOkay -> "确定"
        CommonCancel -> "取消"
        CommonYes -> "是"
        CommonNo -> "否"
        CommonBack -> "返回"
        AlertNeedManualPermissionGrantTitle -> "无权限"
        AlertNeedManualPermissionGrantMessage -> "应用需要您的录音权限，请在系统设置中授予。"
        RecorderScreenCurrentSentenceLabel -> "正在录制："
        else -> null
    }
