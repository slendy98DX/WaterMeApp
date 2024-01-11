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
package com.example.waterme.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.work.WorkManager
import com.example.waterme.BaseApplication
import com.example.waterme.R
import com.example.waterme.database.Plant
import com.example.waterme.viewmodel.PlantViewModel
import com.example.waterme.viewmodel.PlantViewModelFactory
import java.util.concurrent.TimeUnit

class ReminderDialogFragment(private val plant: Plant) : DialogFragment() {

    private val viewModel: PlantViewModel by viewModels {
        PlantViewModelFactory(
            requireActivity().application,
            (requireActivity().application as BaseApplication).database.plantDao()
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            if (!plant.isActive) {
                val builder = AlertDialog.Builder(it)
                    .setTitle("Title")
                    .setMessage("Do you want receive notifications for this ${plant.name}?")
                    .setCancelable(true)
                    .setNegativeButton("No") { _, _ ->
                    }
                    .setPositiveButton("Yes") { _, _ ->
                        viewModel.updatePlantStatus(plant.id)
                        when (plant.schedule) {
                            "daily" -> viewModel.scheduleReminder(24, TimeUnit.HOURS, plant.name)
                            "weekly" -> viewModel.scheduleReminder(7, TimeUnit.DAYS, plant.name)
                            "monthly" -> viewModel.scheduleReminder(30, TimeUnit.DAYS, plant.name)
                        }
                        Log.i("workCEO", "#1" + viewModel.workManager.getWorkInfosByTag("workCEO").toString())
                        Log.i("workCEO", "#2" + "size: " + viewModel.workManager.getWorkInfosByTag("workCEO").get().size.toString())
                    }
                builder.create()
            } else {
                val builder = AlertDialog.Builder(it)
                    .setTitle("Title")
                    .setMessage("Do you want stop receiving notifications for this ${plant.name}?")
                    .setCancelable(true)
                    .setNegativeButton("No") { _, _ ->
                    }
                    .setPositiveButton("Yes") { _, _ ->
                        viewModel.updatePlantStatus(plant.id)
                        viewModel.workManager.cancelUniqueWork(plant.name)

                        Log.i("workCEO", "#3" + viewModel.workManager.getWorkInfosByTagLiveData("workCEO").toString())
                        Log.i("workCEO", "#4" + "size: " + viewModel.workManager.getWorkInfosByTag("workCEO").get().size.toString())
                    }
                builder.create()
            }
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
