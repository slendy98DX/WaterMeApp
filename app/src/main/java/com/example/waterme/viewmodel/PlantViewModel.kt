/*
 * Copyright (C) 2021 The Android Open Source Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.waterme.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.waterme.database.Plant
import com.example.waterme.database.PlantDao
import com.example.waterme.worker.WaterReminderWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class PlantViewModel(application: Application, private val plantDao: PlantDao): ViewModel() {

    fun collectPlants(): Flow<List<Plant>> = plantDao.getAll().flowOn(Dispatchers.IO)
    val workManager = WorkManager.getInstance(application.applicationContext)

    fun updatePlantStatus(id:Long){
        viewModelScope.launch(Dispatchers.IO) {
        plantDao.setStatus(id)
        }
    }

    fun showWorkers(context: Context){
        val runningWorkers = workManager.getWorkInfosByTag("WORKER").get().filter { it.state != WorkInfo.State.CANCELLED }.size
        Toast.makeText(context,"Workers running: $runningWorkers",Toast.LENGTH_SHORT).show()
        Log.d("WorkersList","Worker list size :"+workManager.getWorkInfosByTag("WORKER").get().size + workManager.getWorkInfosByTag("WORKER").get().toString())
    }

    internal fun scheduleReminder(
        duration: Long,
        unit: TimeUnit,
        plantName: String
    ) {
        val data = Data.Builder()
        data.putString(WaterReminderWorker.nameKey, plantName)

        val notification = PeriodicWorkRequestBuilder<WaterReminderWorker>(duration,unit)
            .setInputData(data.build())
            .setInitialDelay(duration,unit)
            .addTag("WORKER")
            .build()


        workManager.enqueueUniquePeriodicWork(
                plantName,
                ExistingPeriodicWorkPolicy.KEEP,
                notification
            )

    }
}


class PlantViewModelFactory(private val application: Application, private val plantDao: PlantDao) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(PlantViewModel::class.java)) {
            PlantViewModel(application,plantDao) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
