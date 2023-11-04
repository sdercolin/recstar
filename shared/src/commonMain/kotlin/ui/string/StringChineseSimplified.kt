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
        CommonMore -> "更多"
        AlertNeedManualPermissionGrantTitle -> "无权限"
        AlertNeedManualPermissionGrantMessage -> "应用需要您的录音权限，请在系统设置中授予。"
        ErrorReadFileFailedMessage -> "无法读取文件。"
        ErrorExportDataFailedMessage -> "导出数据失败。"
        ToastExportDataSuccess -> "导出成功"
        ToastExportDataCancel -> "导出已被取消"
        MainScreenAllSessions -> "所有会话"
        MainScreenNewSession -> "开始新会话"
        MainScreenEmpty -> "暂无会话。"
        SessionScreenCurrentSentenceLabel -> "正在录制："
        SessionScreenActionOpenDirectory -> "打开目录"
        SessionScreenActionExport -> "导出"
        CreateSessionReclistScreenTitle -> "选择录音表"
        CreateSessionReclistScreenActionImport -> "导入录音表"
        CreateSessionReclistScreenEmpty -> "请先导入录音表。"
        CreateSessionReclistScreenContinue -> "完成"
        CreateSessionReclistScreenFailure -> "创建会话失败。"
        else -> null
    }
