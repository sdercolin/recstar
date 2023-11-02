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
        ErrorReadFileFailedMessage -> "无法读取文件。"
        RecorderScreenCurrentSentenceLabel -> "正在录制："
        CreateSessionReclistScreenTitle -> "选择录音表"
        CreateSessionReclistScreenActionImport -> "导入录音表"
        CreateSessionReclistScreenContinue -> "完成"
        CreateSessionReclistScreenFailure -> "创建会话失败。"
        else -> null
    }
