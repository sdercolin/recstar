@file:Suppress("FunctionName")

import repository.AppPreferenceRepository
import repository.AppRecordRepository
import util.appVersionCode

object Migrations {
    fun run(
        recordRepository: AppRecordRepository,
        preferenceRepository: AppPreferenceRepository,
    ) {
        val lastRunVersionCode = recordRepository.value.lastRunVersionCode
        if (lastRunVersionCode <= 14) {
            // on iOS: 14; else: 13
            from_1_0_0_to_1_1_0(preferenceRepository)
        }
        recordRepository.update { copy(lastRunVersionCode = appVersionCode) }
    }

    private fun from_1_0_0_to_1_1_0(preferenceRepository: AppPreferenceRepository) {
        // clear audio device settings to prompt user to reselect and check the new descriptions
        preferenceRepository.update {
            copy(
                desiredInputName = null,
                desiredOutputName = null,
            )
        }
    }
}
