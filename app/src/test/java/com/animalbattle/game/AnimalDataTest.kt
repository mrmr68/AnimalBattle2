package com.animalbattle.game

import com.animalbattle.game.domain.model.AnimalData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AnimalDataTest {

    @Test
    fun `there are exactly 12 animals`() {
        val animals = AnimalData.getAllAnimals()
        assertEquals(12, animals.size)
    }

    @Test
    fun `every animal has 3 abilities`() {
        AnimalData.getAllAnimals().forEach { animal ->
            assertEquals(
                "${animal.id} should have 3 abilities",
                3, animal.abilities.size
            )
        }
    }

    @Test
    fun `every animal has unique id`() {
        val ids = AnimalData.getAllAnimals().map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `every animal has positive baseHp`() {
        AnimalData.getAllAnimals().forEach { animal ->
            assertTrue(
                "${animal.id} baseHp should be positive",
                animal.baseHp > 0
            )
        }
    }

    @Test
    fun `every animal has positive basePower`() {
        AnimalData.getAllAnimals().forEach { animal ->
            assertTrue(
                "${animal.id} basePower should be positive",
                animal.basePower > 0
            )
        }
    }

    @Test
    fun `abilities have increasing damage (weak < medium < strong)`() {
        AnimalData.getAllAnimals().forEach { animal ->
            val abilities = animal.abilities
            assertTrue(
                "${animal.id}: weak damage (${abilities[0].damage}) < medium (${abilities[1].damage})",
                abilities[0].damage < abilities[1].damage
            )
            assertTrue(
                "${animal.id}: medium damage (${abilities[1].damage}) < strong (${abilities[2].damage})",
                abilities[1].damage < abilities[2].damage
            )
        }
    }

    @Test
    fun `abilities have correct XP costs (1, 2, 3)`() {
        AnimalData.getAllAnimals().forEach { animal ->
            val abilities = animal.abilities
            assertEquals("${animal.id} ability 1 XP cost", 1, abilities[0].xpCost)
            assertEquals("${animal.id} ability 2 XP cost", 2, abilities[1].xpCost)
            assertEquals("${animal.id} ability 3 XP cost", 3, abilities[2].xpCost)
        }
    }

    @Test
    fun `getAnimalById returns correct animal`() {
        val lion = AnimalData.getAnimalById("lion")
        assertNotNull(lion)
        assertEquals("lion", lion!!.id)
    }

    @Test
    fun `getAnimalById returns null for unknown id`() {
        val unknown = AnimalData.getAnimalById("unicorn")
        assertEquals(null, unknown)
    }

    @Test
    fun `lion is unlocked by default`() {
        val lion = AnimalData.getAnimalById("lion")
        assertNotNull(lion)
        assertTrue(lion!!.isUnlockedByDefault)
    }

    @Test
    fun `non-lion animals have unlock cost`() {
        AnimalData.getAllAnimals()
            .filter { it.id != "lion" }
            .forEach { animal ->
                assertTrue(
                    "${animal.id} should have unlock cost > 0",
                    animal.unlockCost > 0
                )
            }
    }

    @Test
    fun `all 12 animal types are present`() {
        val expectedIds = setOf(
            "lion", "tiger", "leopard", "cheetah",
            "bear", "wolf", "gorilla", "rhino",
            "elephant", "crocodile", "eagle", "cobra"
        )
        val actualIds = AnimalData.getAllAnimals().map { it.id }.toSet()
        assertEquals(expectedIds, actualIds)
    }
}
