package com.animalbattle.game.domain.model

import com.animalbattle.game.R

object AnimalData {

    fun getAllAnimals(): List<Animal> = listOf(
        createLion(),
        createTiger(),
        createLeopard(),
        createCheetah(),
        createBear(),
        createWolf(),
        createGorilla(),
        createRhino(),
        createElephant(),
        createCrocodile(),
        createEagle(),
        createCobra()
    )

    fun getAnimalById(id: String): Animal? {
        return getAllAnimals().find { it.id == id }
    }

    private fun createLion() = Animal(
        id = "lion",
        nameResId = R.string.animal_lion,
        baseHp = 100,
        basePower = 5,
        unlockCost = 0,
        isUnlockedByDefault = true,
        upgradeCost = 100,
        abilities = listOf(
            Ability("lion_claw", R.string.lion_claw, R.string.lion_claw, 10, 1),
            Ability("lion_roar", R.string.lion_roar, R.string.lion_roar, 20, 2),
            Ability("lion_fury", R.string.lion_fury, R.string.lion_fury, 35, 3)
        )
    )

    private fun createTiger() = Animal(
        id = "tiger",
        nameResId = R.string.animal_tiger,
        baseHp = 90,
        basePower = 6,
        unlockCost = 200,
        upgradeCost = 150,
        abilities = listOf(
            Ability("tiger_strike", R.string.tiger_strike, R.string.tiger_strike, 12, 1),
            Ability("tiger_pounce", R.string.tiger_pounce, R.string.tiger_pounce, 22, 2),
            Ability("tiger_fang", R.string.tiger_fang, R.string.tiger_fang, 38, 3)
        )
    )

    private fun createLeopard() = Animal(
        id = "leopard",
        nameResId = R.string.animal_leopard,
        baseHp = 85,
        basePower = 7,
        unlockCost = 300,
        upgradeCost = 200,
        abilities = listOf(
            Ability("leopard_swipe", R.string.leopard_swipe, R.string.leopard_swipe, 14, 1),
            Ability("leopard_dash", R.string.leopard_dash, R.string.leopard_dash, 25, 2),
            Ability("leopard_ambush", R.string.leopard_ambush, R.string.leopard_ambush, 40, 3)
        )
    )

    private fun createCheetah() = Animal(
        id = "cheetah",
        nameResId = R.string.animal_cheetah,
        baseHp = 75,
        basePower = 8,
        unlockCost = 400,
        upgradeCost = 250,
        abilities = listOf(
            Ability("cheetah_scratch", R.string.cheetah_scratch, R.string.cheetah_scratch, 15, 1),
            Ability("cheetah_sprint", R.string.cheetah_sprint, R.string.cheetah_sprint, 28, 2),
            Ability("cheetah_blitz", R.string.cheetah_blitz, R.string.cheetah_blitz, 42, 3)
        )
    )

    private fun createBear() = Animal(
        id = "bear",
        nameResId = R.string.animal_bear,
        baseHp = 120,
        basePower = 4,
        unlockCost = 500,
        upgradeCost = 300,
        abilities = listOf(
            Ability("bear_swipe", R.string.bear_swipe, R.string.bear_swipe, 8, 1),
            Ability("bear_maul", R.string.bear_maul, R.string.bear_maul, 18, 2),
            Ability("bear_crush", R.string.bear_crush, R.string.bear_crush, 30, 3)
        )
    )

    private fun createWolf() = Animal(
        id = "wolf",
        nameResId = R.string.animal_wolf,
        baseHp = 88,
        basePower = 6,
        unlockCost = 350,
        upgradeCost = 180,
        abilities = listOf(
            Ability("wolf_bite", R.string.wolf_bite, R.string.wolf_bite, 11, 1),
            Ability("wolf_howl", R.string.wolf_howl, R.string.wolf_howl, 21, 2),
            Ability("wolf_pack", R.string.wolf_pack, R.string.wolf_pack, 36, 3)
        )
    )

    private fun createGorilla() = Animal(
        id = "gorilla",
        nameResId = R.string.animal_gorilla,
        baseHp = 130,
        basePower = 5,
        unlockCost = 600,
        upgradeCost = 350,
        abilities = listOf(
            Ability("gorilla_punch", R.string.gorilla_punch, R.string.gorilla_punch, 9, 1),
            Ability("gorilla_slam", R.string.gorilla_slam, R.string.gorilla_slam, 19, 2),
            Ability("gorilla_rage", R.string.gorilla_rage, R.string.gorilla_rage, 32, 3)
        )
    )

    private fun createRhino() = Animal(
        id = "rhino",
        nameResId = R.string.animal_rhino,
        baseHp = 140,
        basePower = 4,
        unlockCost = 700,
        upgradeCost = 400,
        abilities = listOf(
            Ability("rhino_gore", R.string.rhino_gore, R.string.rhino_gore, 7, 1),
            Ability("rhino_charge", R.string.rhino_charge, R.string.rhino_charge, 16, 2),
            Ability("rhino_rampage", R.string.rhino_rampage, R.string.rhino_rampage, 28, 3)
        )
    )

    private fun createElephant() = Animal(
        id = "elephant",
        nameResId = R.string.animal_elephant,
        baseHp = 150,
        basePower = 3,
        unlockCost = 800,
        upgradeCost = 450,
        abilities = listOf(
            Ability("elephant_trunk", R.string.elephant_trunk, R.string.elephant_trunk, 6, 1),
            Ability("elephant_stomp", R.string.elephant_stomp, R.string.elephant_stomp, 14, 2),
            Ability("elephant_tusks", R.string.elephant_tusks, R.string.elephant_tusks, 25, 3)
        )
    )

    private fun createCrocodile() = Animal(
        id = "crocodile",
        nameResId = R.string.animal_crocodile,
        baseHp = 110,
        basePower = 5,
        unlockCost = 550,
        upgradeCost = 280,
        abilities = listOf(
            Ability("croc_bite", R.string.croc_bite, R.string.croc_bite, 10, 1),
            Ability("croc_death_roll", R.string.croc_death_roll, R.string.croc_death_roll, 20, 2),
            Ability("croc_lurk", R.string.croc_lurk, R.string.croc_lurk, 34, 3)
        )
    )

    private fun createEagle() = Animal(
        id = "eagle",
        nameResId = R.string.animal_eagle,
        baseHp = 70,
        basePower = 9,
        unlockCost = 450,
        upgradeCost = 220,
        abilities = listOf(
            Ability("eagle_talon", R.string.eagle_talon, R.string.eagle_talon, 16, 1),
            Ability("eagle_dive", R.string.eagle_dive, R.string.eagle_dive, 30, 2),
            Ability("eagle_storm", R.string.eagle_storm, R.string.eagle_storm, 45, 3)
        )
    )

    private fun createCobra() = Animal(
        id = "cobra",
        nameResId = R.string.animal_cobra,
        baseHp = 80,
        basePower = 7,
        unlockCost = 380,
        upgradeCost = 200,
        abilities = listOf(
            Ability("cobra_spit", R.string.cobra_spit, R.string.cobra_spit, 13, 1),
            Ability("cobra_constrict", R.string.cobra_constrict, R.string.cobra_constrict, 24, 2),
            Ability("cobra_venom", R.string.cobra_venom, R.string.cobra_venom, 38, 3)
        )
    )
}
