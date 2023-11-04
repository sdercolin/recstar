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
        CommonError -> "错误"
        CommonEdit -> "编辑"
        CommonCheck -> "选择"
        AlertNeedManualPermissionGrantTitle -> "无权限"
        AlertNeedManualPermissionGrantMessage -> "应用需要您的录音权限，请在系统设置中授予。"
        ErrorReadFileFailedMessage -> "无法读取文件。"
        ErrorExportDataFailedMessage -> "导出数据失败。"
        ExceptionRenameSessionExisting -> "已存在名为 {0} 的会话。"
        ExceptionRenameSessionInvalid -> "无效的会话名：{0}"
        ExceptionRenameSessionUnexpected -> "重命名会话失败。"
        ToastExportDataSuccess -> "导出成功"
        ToastExportDataCancel -> "导出已被取消"
        MainScreenAllSessions -> "所有会话"
        MainScreenItemSelecting -> "已选 {0} 项"
        MainScreenNewSession -> "开始新会话"
        MainScreenEmpty -> "暂无会话。"
        MainScreenDeleteItemsConfirmationTitle -> "删除会话"
        MainScreenDeleteItemsConfirmationMessage -> "确定要删除 {0} 个会话吗？录音文件将从设备中完全删除。"
        SessionScreenCurrentSentenceLabel -> "正在录制："
        SessionScreenActionOpenDirectory -> "打开目录"
        SessionScreenActionExport -> "导出"
        SessionScreenActionRenameSession -> "重命名会话"
        CreateSessionReclistScreenTitle -> "选择录音表"
        CreateSessionReclistScreenActionImport -> "导入录音表"
        CreateSessionReclistScreenEmpty -> "请先导入录音表。"
        CreateSessionReclistScreenContinue -> "完成"
        CreateSessionReclistScreenFailure -> "创建会话失败。"
        else -> null
    }
