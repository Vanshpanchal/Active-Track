package com.example.fitnessapp

object Constantone {
    fun ExerciseList(): ArrayList<ExerciseModel> {
        val e_list = ArrayList<ExerciseModel>()

        val pushUp = ExerciseModel(1, "PUSH UP ONE", R.drawable.pushup, false, false)
        e_list.add(pushUp)

        val plank = ExerciseModel(2, "PLANK", R.drawable.plank, false, false)
        e_list.add(plank)

        val kneeStretch = ExerciseModel(3, "KNEE STRETCH", R.drawable.kneestrech, false, false)
        e_list.add(kneeStretch)

        val lowerBack =
            ExerciseModel(4, "LOWER BACK STRETCH", R.drawable.lowerbackstrech, false, false)
        e_list.add(lowerBack)

        val squat = ExerciseModel(5, "SQUAT", R.drawable.squat, false, false)
        e_list.add(squat)

        val ballHolding =
            ExerciseModel(6, "BALL HOLDING", R.drawable.chair_posture_holding, false, false)
        e_list.add(ballHolding)

        val backStretch = ExerciseModel(7, "BACK STRETCH", R.drawable.backstrech, false, false)
        e_list.add(backStretch)

        val sitUp = ExerciseModel(8, "SIT UP", R.drawable.situp, false, false)
        e_list.add(sitUp)

        val legStretching =
            ExerciseModel(9, "LEG STRETCHING", R.drawable.leg_stretching, false, false)
        e_list.add(legStretching)

        val stretch = ExerciseModel(10, "STRETCH", R.drawable.stretch, false, false)
        e_list.add(stretch)

        val elbow = ExerciseModel(11, "ELBOW STRETCHING", R.drawable.elbow, false, false)
        e_list.add(elbow)

        val neck = ExerciseModel(12, "NECK STRETCH", R.drawable.neck, false, false)
        e_list.add(neck)
        return e_list
    }
}