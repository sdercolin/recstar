package ui.string

import const.APP_NAME
import ui.string.Strings.*

fun Strings.ko(): String =
    when (this) {
        CommonOkay -> "확인"
        CommonCancel -> "취소"
        CommonYes -> "예"
        CommonNo -> "아니요"
        CommonBack -> "뒤로가기"
        CommonMore -> "더보기"
        CommonError -> "오류"
        CommonEdit -> "편집"
        CommonCheck -> "확인"
        CommonImport -> "불러오기"
        CommonDoNotShowAgain -> "다시 보지 않기"
        CommonNoMatch -> "검색 결과 없음"
        AlertNeedManualPermissionGrantTitle -> "권한이 필요합니다."
        AlertNeedManualPermissionGrantMessage ->
            "녹음 권한이 필요합니다. 시스템 설정에서 녹음 권한을 활성화 해주세요."
        ErrorReadFileFailedMessage -> "파일 읽기를 실패했습니다."
        ErrorExportDataFailedMessage -> "데이터 추출을 실패했습니다."
        ExceptionRenameSessionExisting -> "'{0}'은 이미 있는 세션 이름입니다."
        ExceptionRenameSessionInvalid -> "올바르지 않은 세션 이름: {0}"
        ExceptionRenameSessionUnexpected -> "세션 이름 변경을 실패했습니다."
        ExceptionUnsupportedAudioFormat ->
            "현재의 오디오 형식을 지원하는 오디오 장치를 찾지 못하였습니다: {0}, " +
                "오디오 설정, 샘플 레이트, Bit Depth를 확인해주세요."
        ExceptionUnsupportedAudioDevice ->
            "선택한 오디오 장치를 사용할 수 없습니다. 오디오 설정을 확인해 주세요. " +
                "지속적으로 오류가 발생하는 경우, 시스템의 기본 오디오 장치를 선택한 뒤, " +
                "RecStar 설정에서 사용하실 오디오 장치를 선택 하세요."
        ExceptionReclistNotFound ->
            "사용된 녹음 리스트 ({0}) 혹은 코멘트를 찾지 못했습니다." +
                "세션을 사용할려면 녹음 리스트 혹은 코멘트를 다시 불러와 주세요."
        ExceptionReclistNoValidLine ->
            "해당 녹음 리스트에 올바르지 않은 헹이 포함되어 있습니다." +
                "파일에 사용할 수 없는 문자가 포함되어 있는지 확인해주세요."
        ExceptionTextDecodeFailure ->
            "텍스트 디코딩에 실패하였습니다. 설정에서 '텍스트 인코딩 물어보기'를 활성화 후 다시 시도해주세요."
        AlertExportTips ->
            "이 녹음 리스트에는 일본어 탁점기호가 포함되어 있습니다." +
                "MacOS/iOS 구조상 이러한 파일은 특수한 인코딩 방법으로 저장되며, " +
                "다른 OS에서는 올바르게 인식되지 않을 수 있습니다." +
                " " +
                "다른 OS상의 소프트웨어에서 이 파일을 사용하기 전에 " +
                "파일의 이름을 NFC 형식으로 바꿔주세요. " +
                "\"vLabeler\"의 \"Tools\" 메뉴의 \"File Name Normalizer\"를 사용해 변경 가능합니다."
        AlertExportNoData -> "내보낼 데이터가 없습니다."
        ToastExportDataSuccess -> "내보내기에 성공 하였습니다."
        ToastExportDataCancel -> "내보내기가 취소 되었습니다."
        ToastImportSuccess -> "불러오기에 성공 하였습니다."
        ToastImportFailure -> "불러오기에 실패 하였습니다."
        AlertUnexpectedErrorOpenLog -> "오류가 발생하였습니다. 로그 파일과 함께 개발자에게 알려주세요."
        AlertUnexpectedErrorOpenLogButton -> "로그 폴더 열기"
        AlertUnexpectedErrorCopyLogToClipboard ->
            "오류가 발생하였습니다. 로그 파일과 함께 개발자에게 알려주세요."
        AlertUnexpectedErrorCopyLogToClipboardButton -> "오류 정보 복사"
        MainScreenAllSessions -> "세션"
        MainScreenNewSession -> "새로운 세션 시작"
        MainScreenEmpty -> "아직 세션이 없습니다."
        MainScreenDeleteItemsTitle -> "세션 삭제"
        MainScreenDeleteItemsMessage ->
            "정말 세션 {0}개를 삭제 하시겠습니까? 녹음한 문장을 영원히 복구할 수 없게 됩니다."
        SessionScreenCurrentSentenceLabel -> "녹음중: "
        SessionScreenCommentEmpty -> "코멘트 없음"
        SessionScreenNoData -> "데이터 없음"
        SessionScreenActionOpenDirectory -> "폴더 열기"
        SessionScreenActionExport -> "내보내기"
        SessionScreenActionRenameSession -> "세션 이름 변경"
        SessionScreenActionConfigureGuideAudio -> "가이드 BGM 설정하기"
        SessionScreenActionSkipFinishedSentence -> "녹음한 문장 건너뛰기"
        SessionScreenTogglePlaying -> "녹음 재생"
        SessionScreenNoGuideAudio -> "BGM 없음"
        SessionScreenAlertGuideAudioNotFoundMessage ->
            "가이드 BGM을 찾지 못하였습니다. BGM을 다시 불러와 주세요."
        CreateSessionReclistScreenTitle -> "사용할 리스트를 선택 해주세요."
        CreateSessionReclistScreenActionImport -> "리스트 불러오기"
        CreateSessionReclistScreenActionImportCommentAlertMessage ->
            "녹음 리스트의 코멘트 파일도 불러오시겠습니까? (-comment.txt)"
        CreateSessionReclistScreenAllReclists -> "녹음 리스트"
        CreateSessionReclistScreenEmpty -> "녹음 리스트를 먼저 불러와 주세요."
        CreateSessionReclistScreenContinue -> "완료"
        CreateSessionReclistScreenFailure -> "세션 제작을 실패하였습니다."
        CreateSessionReclistScreenDeleteItemsTitle -> "리스트 삭제"
        CreateSessionReclistScreenDeleteItemsMessage -> "정말 리스트 {0}개를 삭제하시겠습니까?"
        GuideAudioScreenTitle -> "가이드 BGM 설정"
        GuideAudioScreenActionImport -> "가이드 BGM 불러오기"
        GuideAudioScreenActionImportConfigAlertMessage ->
            "해당 가이드 BGM의 설정파일을 가져오시겠습니까? (.txt)"
        GuideAudioScreenAllGuideAudios -> "사용할 가이드 BGM을 선택 해주세요."
        GuideAudioScreenEmpty -> "가이드 BGM을 먼저 불러와 주세요."
        GuideAudioScreenDeleteItemsTitle -> "가이드 BGM 삭제"
        GuideAudioScreenDeleteItemsMessage -> "정말 가이드 BGM {0}개를 삭제하시겠습니까?"
        ItemSelectingTitle -> "{0}개 선택"
        SelectedLabel -> "선택됨"
        SearchBar -> "검색"
        SearchBarClear -> "초기화"
        SortingMethod -> "정렬하기"
        SortingMethodNameAsc -> "이름순 (오름차순)"
        SortingMethodNameDesc -> "이름순 (내림차순)"
        SortingMethodUsedAsc -> "마지막으로 사용한순 (오름차순)"
        SortingMethodUsedDesc -> "마지막으로 사용한순 (내림차순)"
        PreferenceScreenTitle -> "설정"
        PreferenceGroupAppearance -> "외형"
        PreferenceLanguage -> "언어"
        PreferenceLanguageAuto -> "자동"
        PreferenceTheme -> "테마"
        PreferenceThemeSystem -> "시스템 테마 따라가기"
        PreferenceThemeLight -> "라이트 모드"
        PreferenceThemeDark -> "다크 모드"
        PreferenceOrientation -> "화면 방향"
        PreferenceOrientationAuto -> "자동"
        PreferenceOrientationPortrait -> "세로"
        PreferenceOrientationLandscape -> "가로"
        PreferenceGroupRecording -> "녹음"
        PreferenceContinuousRecording -> "연속 모드"
        PreferenceContinuousRecordingDescription -> "가이드 BGM 기준"
        PreferenceTrimRecording -> "녹음 다듬기"
        PreferenceTrimRecordingDescription -> "가이드 BGM 기준"
        PreferenceRecordWhileHolding -> "누르는 동안 녹음하기"
        PreferenceRecordingShortKey -> "녹음 단축키"
        PreferenceRecordingShortKeyEnter -> "Enter"
        PreferenceRecordingShortKeyR -> "R"
        PreferenceAutoListenBack -> "녹음 후, 자동으로 녹음 듣기"
        PreferenceAutoListenBackDescription -> "연속 모드에서는 사용할 수 없습니다."
        PreferenceAutoNext -> "녹음 후, 자동으로 다음 문장으로 넘어가기"
        PreferenceAutoNextDescription -> "연속 모드에서는 사용할 수 없습니다."
        PreferenceGroupAudio -> "오디오"
        PreferenceGroupAudioDescription ->
            "시스템 기본 오디오 장치를 사용하는 것을 추천드립니다. " +
                "RecStar에서 장치를 변경 하는 대신,  " +
                "시스템 설정에서 장치를 변경해주세요."
        PreferenceInputDeviceName -> "입력 기기"
        PreferenceOutputDeviceName -> "출력 기기"
        PreferenceSampleRate -> "샘플 레이트"
        PreferenceBitDepth -> "Bit Depth"
        PreferenceDeviceNameNotFoundTemplate -> "{0} (찾지 못함)"
        PreferenceGroupView -> "보기"
        PreferenceTitleBarStyle -> "타이틀 바 스타일"
        PreferenceTitleBarStyleFileName -> "파일 이름만 보기"
        PreferenceTitleBarStyleFileNameWithComment -> "파일 이름과 함께 작게 코멘트 보기"
        PreferenceTitleBarStyleCommentWithFileName -> "코멘트와 함께 작게 파일 이름 보기"
        PreferenceTitleBarStyleComment -> "코멘트만 보기"
        PreferenceGroupMisc -> "기타"
        PreferenceAlwaysConfirmTextEncoding -> "텍스트 인코딩 물어보기"
        PreferenceContentRootLocation -> "콘텐츠를 저장할 폴더"
        PreferenceAbout -> "$APP_NAME 정보"
        AboutScreenPrivacyPolicy -> "개인정보 정책"
        AboutScreenCopyDeviceInfo -> "기기 정보 복사"
        AboutScreenDeviceInfoCopied -> "기기의 정보가 클립보드에 복사되었습니다."
        AboutScreenViewLicenses -> "라이센스 보기"
        AboutScreenViewOnGithub -> "깃허브 보기"
        LicenseScreenTitle -> "라이센스"
        MenuFile -> "파일"
        MenuFileNewSession -> "새 세션 시작"
        MenuFileImportReclist -> "녹음 리스트 불러오기"
        MenuFileImportGuideAudio -> "가이드 BGM 불러오기"
        MenuFileOpenDirectory -> "폴더 열기"
        MenuFileBack -> "뒤로"
        MenuEdit -> "편집"
        MenuEditRenameSession -> "세션 이름 변경"
        MenuEditConfigureGuideAudio -> "가이드 BGM 수정하기"
        MenuEditEditList -> "리스트 편집"
        MenuAction -> "행동"
        MenuActionNextSentence -> "다음 문장으로 이동"
        MenuActionPreviousSentence -> "이전 문장으로 이동"
        MenuActionToggleRecording -> "녹음 시작"
        MenuActionToggleRecordingHoldingMode -> "녹음 시작 (단축키만 가능)"
        MenuSettings -> "설정"
        MenuSettingsOpenSettings -> "설정 열기"
        MenuSettingsClearSettings -> "모든 설정 초기화"
        MenuSettingsClearSettingsAlertMessage -> "정말 모든 설정을 초기화 하시겠습니까?"
        MenuSettingsClearAppData -> "앱 데이터 지우기"
        MenuSettingsClearAppDataAlertMessage ->
            "설정 및 사용 기록들을 포함한 모든 앱 데이터를 삭제하시겠습니까? " +
                "세션과 가져온 녹음 리스트, BGM은 삭제되지 않습니다. " +
                "삭제 후, 앱이 종료됩니다."
        MenuHelp -> "도움말"
        MenuHelpOpenContentDirectory -> "콘텐츠 폴더 열기"
        MenuHelpOpenAppDirectory -> "앱 폴더 열기"
        MenuHelpAbout -> "알아보기"
        TextEncodingDialogTitle -> "인코딩 설정"
        TextEncodingDialogEncodingLabel -> "인코딩"
        TextEncodingDialogEncodingAuto -> "자동"
        TextEncodingDialogEncodingError -> "디코딩 오류"
    }
