package ui.string

import const.APP_NAME
import ui.string.Strings.*

fun Strings.ru(): String =
    when (this) {
        CommonOkay -> "ОК"
        CommonCancel -> "Отмена"
        CommonYes -> "Да"
        CommonNo -> "Нет"
        CommonBack -> "Назад"
        CommonMore -> "Подробнее"
        CommonError -> "Ошибка"
        CommonEdit -> "Редактировать"
        CommonCheck -> "Проверить"
        CommonImport -> "Импортировать"
        CommonDoNotShowAgain -> "Не показывать снова"
        CommonNoMatch -> "Нет совпадений"
        AlertNeedManualPermissionGrantTitle -> "Требуется разрешение"
        AlertNeedManualPermissionGrantMessage ->
            "Приложению требуется ваше разрешение на запись звука. Пожалуйста, дайте разрешение в системных настройках."
        ErrorReadFileFailedMessage -> "Не удалось прочитать файл."
        ErrorExportDataFailedMessage -> "Не удалось экспортировать данные"
        ExceptionRenameSessionExisting -> "Сессия с названием {0} уже существует."
        ExceptionRenameSessionInvalid -> "Неверное название сессии: {0}"
        ExceptionRenameSessionUnexpected -> "Не удалось переименовать сессию."
        ExceptionUnsupportedAudioFormat ->
            "Не удалось найти ни одного аудиоустройства, поддерживающего текущий аудиоформат: {0}, " +
                "проверьте и отрегулируйте настройки звука, особенно частоту дискретизации и битовую глубину."
        ExceptionUnsupportedAudioDevice ->
            "Выбранное аудиоустройство не может быть использовано, проверьте настройки звука " +
                "на стороне вашей системы. Если эта ошибка повторяется, попробуйте выбрать аудиоустройства по умолчанию " +
                "в настройках RecStar и выберите нужные аудиоустройства в системных настройках."
        ExceptionReclistNotFound ->
            "Не удалось открыть сессию, потому что не найден реклист ({0}) или его файл комментариев, используемый в этой сессии " +
                "не найден. Пожалуйста, импортируйте их заново, чтобы использовать эту сессию."
        ExceptionReclistNoValidLine ->
            "Файл реклиста не содержит ни одной допустимой строки. " +
                "Убедитесь, что строки не содержат недопустимых символов, которые нельзя использовать в именах файлов."
        ExceptionTextDecodeFailure ->
            "Не удалось декодировать текст. Пожалуйста, включите `Спрашивать кодировку текста` в настройках и повторите попытку с правильной кодировкой."
        AlertExportTips ->
            "Ваш реклист содержит предложения с японскими голосовыми знаками, " +
                "которые будут сохранены в специальной кодировке, " +
                "из-за чего не смогут быть корректно распознаны на других платфорамх, " +
                "в связи с особенностями MacOS/iOS. " +
                "Прежде чем использовать их на других платформах с другими программами, " +
                "пожалуйста, нормализуйте имена файлов к форме NFC. " +
                "Для этого можно использовать инструмент \"File Name Normalizer\" в меню \"Tools\" приложения \"vLabeler\"."
        AlertExportNoData -> "Нет данных для экспорта"
        ToastExportDataSuccess -> "Успешное экспортирование"
        ToastExportDataCancel -> "Экспортирование отменено"
        ToastImportSuccess -> "Успешно импортировано"
        ToastImportFailure -> "Не удалось импортировать"
        AlertUnexpectedErrorOpenLog -> "Произошла ошибка. Пожалуйста, сообщите об этом разработчику, предоставив файл журнала ошибок."
        AlertUnexpectedErrorOpenLogButton -> "Открыть папку с журналом"
        AlertUnexpectedErrorCopyLogToClipboard ->
            "Произошла ошибка. Пожалуйста, сообщите разработчику информацию об ошибке."
        AlertUnexpectedErrorCopyLogToClipboardButton -> "Скопировать информацию об ошибке"
        MainScreenAllSessions -> "Сессии"
        MainScreenNewSession -> "Начать новую сессию"
        MainScreenEmpty -> "Нет сессий"
        MainScreenDeleteItemsTitle -> "Удалить сессии"
        MainScreenDeleteItemsMessage ->
            "Вы уверены, что хотите удалить {0} сессию(-ии)? Записи будут полностью удалены с устройства."
        SessionScreenCurrentSentenceLabel -> "Текущая запись: "
        SessionScreenCommentEmpty -> "Нет комментария."
        SessionScreenNoData -> "Нет данных."
        SessionScreenActionOpenDirectory -> "Открыть директорю"
        SessionScreenActionExport -> "Экспортировать"
        SessionScreenActionRenameSession -> "Переименовать сессию"
        SessionScreenActionConfigureGuideAudio -> "Настроить сопровождающий BGM"
        SessionScreenActionSkipFinishedSentence -> "Пропускать записанные предложения"
        SessionScreenTogglePlaying -> "Переключить воспроизведение"
        SessionScreenNoGuideAudio -> "Не указано"
        SessionScreenAlertGuideAudioNotFoundMessage ->
            "Файл сопровождающего BGM не найден. Пожалуйста, сконфигурируйте сопровождающий BGM заново."
        CreateSessionReclistScreenTitle -> "Выберите реклист для записи"
        CreateSessionReclistScreenActionImport -> "Импортировать реклист"
        CreateSessionReclistScreenActionImportCommentAlertMessage ->
            "Хотите ли вы импортировать соответствующий файл комментариев (-comment.txt)?"
        CreateSessionReclistScreenAllReclists -> "Реклисты"
        CreateSessionReclistScreenEmpty -> "Пожалуйста, сначала импортируйте реклист."
        CreateSessionReclistScreenContinue -> "Закончить"
        CreateSessionReclistScreenFailure -> "Не удалось создать сессию."
        CreateSessionReclistScreenDeleteItemsTitle -> "Удалить реклисты"
        CreateSessionReclistScreenDeleteItemsMessage -> "Вы уверены, что хотите удалить {0} реклист(а/ов)?"
        GuideAudioScreenTitle -> "Параметры сопровождающего BGM"
        GuideAudioScreenActionImport -> "Импортировать сопровождающий BGM"
        GuideAudioScreenActionImportConfigAlertMessage ->
            "Хотите ли вы импортировать соответствующий файл конфигурации сопровождающего BGM (.txt)?"
        GuideAudioScreenAllGuideAudios -> "Сопровождающие BGMы"
        GuideAudioScreenEmpty -> "Пожалуйста, сначала импортируйте сопровождающий BGM."
        GuideAudioScreenDeleteItemsTitle -> "Удалить сопровождающие BGMы"
        GuideAudioScreenDeleteItemsMessage -> "Вы уверены, что хотите удалить {0} сопровождающий BGM(а/ы)?"
        ItemSelectingTitle -> "Выбран(о) {0} элемент(а/ов)"
        SelectedLabel -> "Выбрано"
        SearchBar -> "Поиск"
        SearchBarClear -> "Очистить"
        SortingMethod -> "Метод сортировки"
        SortingMethodNameAsc -> "Название (возрастание)"
        SortingMethodNameDesc -> "Название (убывание)"
        SortingMethodUsedAsc -> "Последнее (возрастание)"
        SortingMethodUsedDesc -> "Последнее (убывание)"
        PreferenceScreenTitle -> "Параметры"
        PreferenceGroupAppearance -> "Внешний вид"
        PreferenceLanguage -> "Язык"
        PreferenceLanguageAuto -> "Авто"
        PreferenceTheme -> "Тема"
        PreferenceThemeSystem -> "Как в системе"
        PreferenceThemeLight -> "Светлая"
        PreferenceThemeDark -> "Темная"
        PreferenceOrientation -> "Ориентация экрана"
        PreferenceOrientationAuto -> "Автоматическая"
        PreferenceOrientationPortrait -> "Портретная"
        PreferenceOrientationLandscape -> "Альбомная"
        PreferenceGroupRecording -> "Запись"
        PreferenceContinuousRecording -> "Продолжительная запись"
        PreferenceContinuousRecordingDescription -> "На базе сопроводительного BGM"
        PreferenceTrimRecording -> "Обрезка записей"
        PreferenceTrimRecordingDescription -> "На базе сопроводительного BGM"
        PreferenceRecordWhileHolding -> "Удерживать для записи"
        PreferenceRecordingShortKey -> "Ярлык для записи"
        PreferenceRecordingShortKeyEnter -> "Enter"
        PreferenceRecordingShortKeyR -> "R"
        PreferenceAutoListenBack -> "Автоматическое прослушивание после записи"
        PreferenceAutoListenBackDescription -> "Недоступно в продолжительной записи"
        PreferenceAutoNext -> "Автоматически идти дальше после записи"
        PreferenceAutoNextDescription -> "Недоступно в продолжительной записи"
        PreferenceGroupAudio -> "Аудио"
        PreferenceGroupAudioDescription ->
            "Рекомендуется всегда использовать системные аудиоустройства " +
                "по умолчанию и выбирать нужные аудиоустройства в " +
                "системных настройках, а не изменять настройки " +
                "аудиоустройств здесь"
        PreferenceInputDeviceName -> "Устройство ввода"
        PreferenceOutputDeviceName -> "Устройство вывода"
        PreferencePreferBuiltInMicrophone -> "Предпочтение встроенному микрофону"
        PreferenceSampleRate -> "Частота дискретизации"
        PreferenceBitDepth -> "Разрядность"
        PreferenceDeviceNameNotFoundTemplate -> "{0} (Не найдено)"
        PreferenceGroupView -> "Вид"
        PreferenceTitleBarStyle -> "Стиль заголовка"
        PreferenceTitleBarStyleFileName -> "Только название файла"
        PreferenceTitleBarStyleFileNameWithComment -> "Название файла с комментарием (маленькое)"
        PreferenceTitleBarStyleCommentWithFileName -> "Комментарий с названем файла (маленькое)"
        PreferenceTitleBarStyleComment -> "Только комментарий"
        PreferenceGroupMisc -> "Прочее"
        PreferenceAlwaysConfirmTextEncoding -> "Спрашивать кодировку файла"
        PreferenceContentRootLocation -> "Расположение каталога содержимого"
        PreferenceAbout -> "О $APP_NAME"
        AboutScreenPrivacyPolicy -> "Политика конфиденциальности"
        AboutScreenCopyDeviceInfo -> "Скопировать информацию об устройстве"
        AboutScreenDeviceInfoCopied -> "Информация об устройстве скопирована в буфер обмена"
        AboutScreenViewLicenses -> "Просмотреть лицензии"
        AboutScreenViewOnGithub -> "Посмотреть на GitHub"
        LicenseScreenTitle -> "Лицензии"
        MenuFile -> "Файл"
        MenuFileNewSession -> "Начать новую сессию"
        MenuFileImportReclist -> "Импортировать реклист"
        MenuFileImportGuideAudio -> "Импортировать сопроводительный BGM"
        MenuFileOpenDirectory -> "Открыть директорию"
        MenuFileBack -> "Назад"
        MenuEdit -> "Правка"
        MenuEditRenameSession -> "Переименовать сессию"
        MenuEditConfigureGuideAudio -> "Настройка сопроводительного BGM"
        MenuEditEditList -> "Редактировать список"
        MenuAction -> "Действие"
        MenuActionNextSentence -> "Следующее предложение"
        MenuActionPreviousSentence -> "Предыдущее предложение"
        MenuActionToggleRecording -> "Переключатель записи"
        MenuActionToggleRecordingHoldingMode -> "Переключатель записи (Доступен только ярлык)"
        MenuSettings -> "Настройки"
        MenuSettingsOpenSettings -> "Открыть настройки"
        MenuSettingsClearSettings -> "Сбросить все настройки"
        MenuSettingsClearSettingsAlertMessage -> "Вы уверены что хотите сбросить все настройки?"
        MenuSettingsClearAppData -> "Очистить данные приложения"
        MenuSettingsClearAppDataAlertMessage ->
            "Вы уверены, что хотите очистить все данные приложения (включая настройки и записи об использовании)? " +
                "Это не приведет к удалению ваших сессий и импортированных ресурсов. " +
                "Приложение закроется после очистки."
        MenuHelp -> "Справка"
        MenuHelpOpenContentDirectory -> "Открыть директорию содержимого"
        MenuHelpOpenAppDirectory -> "Открыть директорию приложения"
        MenuHelpAbout -> "О программе"
        TextEncodingDialogTitle -> "Настройка кодировки"
        TextEncodingDialogEncodingLabel -> "Кодировка"
        TextEncodingDialogEncodingAuto -> "Авто"
        TextEncodingDialogEncodingError -> "Ошибка декодирования"
    }
