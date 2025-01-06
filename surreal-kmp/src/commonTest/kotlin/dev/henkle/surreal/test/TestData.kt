package dev.henkle.surreal.test

import dev.henkle.surreal.internal.utils.nullSerializer
import dev.henkle.surreal.test.models.Country
import dev.henkle.surreal.test.models.Genre
import dev.henkle.surreal.test.models.Medium
import dev.henkle.surreal.test.models.Person
import dev.henkle.surreal.test.models.Series
import dev.henkle.surreal.types.SurrealRecord
import kotlinx.serialization.encodeToString

object TestData {
    val sliceOfLife = Genre(name = "Slice of Life")
    val drama = Genre(name = "Drama")
    val fantasy = Genre(name = "Fantasy")
    val comedy = Genre(name = "Comedy")
    val sciFi = Genre(name = "Sci-Fi")
    val genres = listOf(sliceOfLife, drama, fantasy, comedy, sciFi)

    val us = Country(name = "United States of America")
    val japan = Country(name = "Japan")
    val pact = Country(name = "Land of the Pact")
    val earth = Country(name = "Government of Earth")
    val candy = Country(name = "Candy Kingdom")
    val tempest = Country(name = "Jura Tempest Federation")
    val hebrion = Country(name = "Hebrion Empire")
    val sanMagnolia = Country(name = "Republic of San Magnolia")
    val mars = Country(name = "Mars")
    val countries = listOf(us, japan, pact, earth, candy, tempest, hebrion, sanMagnolia, mars)

    val existence = Medium(name = "Existence")
    val lightNovel = Medium(name = "Light Novel")
    val webNovel = Medium(name = "Web Novel")
    val manga = Medium(name = "Manga")
    val anime = Medium(name = "Anime")
    val animation = Medium(name = "Animation")
    val mediums = listOf(existence, lightNovel, webNovel, manga, anime, animation)

    val realWorld = Series(
        name = "Real World",
        primaryMedium = existence.id,
        genre = sliceOfLife.id,
    )
    val oshiNoKo = Series(
        name = "Oshi no Ko",
        primaryMedium = manga.id,
        genre = drama.id,
    )
    val witchHat = Series(
        name = "Witch Hat Atelier",
        primaryMedium = manga.id,
        genre = fantasy.id,
    )
    val futurama = Series(
        name = "Futurama",
        primaryMedium = animation.id,
        genre = comedy.id,
    )
    val adventureTime = Series(
        name = "Adventure Time",
        primaryMedium = animation.id,
        genre = fantasy.id,
    )
    val tensura = Series(
        name = "Tensei Shitara Slime Datta Ken",
        primaryMedium = lightNovel.id,
        genre = fantasy.id,
    )
    val returners = Series(
        name = "A Returner's Magic Should Be Special",
        primaryMedium = webNovel.id,
        genre = fantasy.id,
    )
    val eightySix = Series(
        name = "86: Eighty-Six",
        primaryMedium = lightNovel.id,
        genre = drama.id,
    )
    val cowboyBebop = Series(
        name = "Cowboy Bebop",
        primaryMedium = anime.id,
        genre = sciFi.id,
    )
    val series = listOf(realWorld, oshiNoKo, witchHat, futurama, adventureTime, tensura, returners, eightySix, cowboyBebop)

    val garrison = Person(
        firstName = "Garrison",
        lastName = "Henkle",
        country = us.id,
        series = realWorld.id,
    )
    val akane = Person(
        firstName = "Akane",
        lastName = "Kurokawa",
        country = japan.id,
        series = oshiNoKo.id,
    )
    val qifrey = Person(
        firstName = "Qifrey",
        lastName = "",
        country = pact.id,
        series = witchHat.id,
    )
    val fry = Person(
        firstName = "Philip",
        lastName = "Fry",
        country = earth.id,
        series = futurama.id,
    )
    val finn = Person(
        firstName = "Finn",
        lastName = "The Human",
        country = candy.id,
        series = adventureTime.id,
    )
    val rimuru = Person(
        firstName = "Rimuru",
        lastName = "Tempest",
        country = tempest.id,
        series = tensura.id,
    )
    val desir = Person(
        firstName = "Desir",
        lastName = "Herrman",
        country = hebrion.id,
        series = returners.id,
    )
    val shin = Person(
        firstName = "Shin",
        lastName = "Nouzen",
        country = sanMagnolia.id,
        series = eightySix.id,
    )
    val spike = Person(
        firstName = "Spike",
        lastName = "Spiegel",
        country = mars.id,
        series = cowboyBebop.id,
    )
    val people = listOf(garrison, akane, qifrey, fry, finn, rimuru, desir, shin, spike)

    fun asSQLInsertStatements(): Array<String> = arrayOf(
        genres.convertToInsertSQL(),
        countries.convertToInsertSQL(),
        mediums.convertToInsertSQL(),
        series.convertToInsertSQL(),
        people.convertToInsertSQL(),
    )

    private inline fun <reified R: SurrealRecord<R>> List<R>.convertToInsertSQL(): String =
        insert(
            table = first().tableName,
            records = joinToString(prefix = "[", postfix = "]") {
                nullSerializer
                    .encodeToString(value = it)
                    .stripTableFromId()
                    .escapeRecordLinks()
            },
        )

    private fun insert(table: String, records: String) = "insert into $table $records;"
    private val escapeRecordLinksRegex = """([,{]"\w+?":)("\w+?:[\d\S]+?")""".toRegex()
    private fun String.escapeRecordLinks(): String = replace(
        regex = escapeRecordLinksRegex,
        replacement = "$1r$2",
    )
    private val tableFromIdRegex = """id":"\S+?:(.+?)"""".toRegex()
    private fun String.stripTableFromId() = replaceFirst(
        regex = tableFromIdRegex,
        replacement = """id":"$1"""",
    )
}
