package com.example.fitnessapp

object Constantone {
    fun ExerciseList(): ArrayList<ExerciseModel> {
        val e_list = ArrayList<ExerciseModel>()

        val pushUp = ExerciseModel(1, "HASTASANA", R.drawable.hastasana, false, false)
        e_list.add(pushUp)

        val plank = ExerciseModel(2, "UTTANASANA", R.drawable.uttanasana, false, false)
        e_list.add(plank)

        val kneeStretch = ExerciseModel(3, "TRIKONASANA", R.drawable.trikonasana, false, false)
        e_list.add(kneeStretch)

        val lowerBack =
            ExerciseModel(4, "UTKATASANA", R.drawable.utkatasana, false, false)
        e_list.add(lowerBack)

        val squat = ExerciseModel(5, "ADHO MUKHA SVANASANA", R.drawable.adhomukhasvanasana, false, false)
        e_list.add(squat)

        val ballHolding =
            ExerciseModel(6, "PASCHIMOTTANASANA", R.drawable.paschimottanasana, false, false)
        e_list.add(ballHolding)

        val backStretch = ExerciseModel(7, "AGNISTAMBHASANA", R.drawable.agnistambhasana, false, false)
        e_list.add(backStretch)

        val sitUp = ExerciseModel(8, "MATSYENDRASANA", R.drawable.matsyendrasana, false, false)
        e_list.add(sitUp)

        val legStretching =
            ExerciseModel(9, "SETU BANDHA SARVANGASANA CHAKRASANA", R.drawable.setubandhasarvangasanachakrasana, false, false)
        e_list.add(legStretching)

        val stretch = ExerciseModel(10, "VRKSASANA", R.drawable.vrksasana, false, false)
        e_list.add(stretch)

        val elbow = ExerciseModel(11, "APANASANA", R.drawable.apanasana, false, false)
        e_list.add(elbow)

        val neck = ExerciseModel(12, "PRANAYAMA", R.drawable.pranayama, false, false)
        e_list.add(neck)
        return e_list
    }
}