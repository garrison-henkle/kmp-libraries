package dev.henkle.korvus.utils

import dev.henkle.nanoid.nanoId

object TestData {
    val genreSliceOfLife = Genre(id = "Genre/${nanoId()}", name = "Slice of Life")
    val genreDrama = Genre(name = "Drama")
    val genreFantasy = Genre(name = "Fantasy")
    val genreComedy = Genre(name = "Comedy")
    val genreSciFi = Genre(name = "Sci-Fi")

    val countryUS = Country(id = "Country/${nanoId()}", name = "United States of America")
    val countryJP = Country(name = "Japan")
    val countryPact = Country(name = "Land of the Pact")
    val countryEarth = Country(name = "Government of Earth")
    val countryCandy = Country(name = "Candy Kingdom")
    val countryTempest = Country(name = "Jura Tempest Federation")
    val countryHebrion = Country(name = "Hebrion Empire")
    val countrySanMagnolia = Country(name = "Republic of San Magnolia")
    val countryMars = Country(name = "Mars")

    val series1 = Series(
        id = "Series/${nanoId()}",
        name = "Real World",
        primaryMedium = "Existence",
        genre = id(record = genreSliceOfLife),
    )

    val series2 = Series(
        id = nanoId(),
        name = "Oshi no Ko",
        primaryMedium = "Manga",
        genre = id(record = genreDrama),
    )

    val series3 = Series(
        id = nanoId(),
        name = "Witch Hat Atelier",
        primaryMedium = "Manga",
        genre = id(record = genreFantasy),
    )

    val series4 = Series(
        id = nanoId(),
        name = "Futurama",
        primaryMedium = "Animation",
        genre = id(record = genreComedy),
    )

    val series5 = Series(
        id = nanoId(),
        name = "Adventure Time",
        primaryMedium = "Animation",
        genre = id(record = genreFantasy),
    )

    val series6 = Series(
        id = nanoId(),
        name = "Tensei Shitara Slime Datta Ken",
        primaryMedium = "Light Novel",
        genre = id(record = genreFantasy),
    )

    val series7 = Series(
        id = nanoId(),
        name = "A Returner's Magic Should Be Special",
        primaryMedium = "Web Novel",
        genre = id(record = genreFantasy),
    )

    val series8 = Series(
        id = nanoId(),
        name = "86: Eighty-Six",
        primaryMedium = "Light Novel",
        genre = id(record = genreDrama),
    )

    val series9 = Series(
        id = nanoId(),
        name = "Cowboy Bebop",
        primaryMedium = "Anime",
        genre = id(record = genreSciFi),
    )

    val person1 = Person(
        id = "1",
        firstName = "Garrison",
        lastName = "Henkle",
        country = id(record = countryUS),
        series = id(record = series1),
    )
    val person2 = Person(
        id = "2",
        firstName = "Akane",
        lastName = "Kurokawa",
        country = id(record = countryJP),
        series = id(record = series2),
    )
    val person3 = Person(
        id = "3",
        firstName = "Qifrey",
        lastName = "",
        country = id(record = countryPact),
        series = id(record = series3),
    )
    val person4 = Person(
        id = "4",
        firstName = "Philip",
        lastName = "Fry",
        country = id(record = countryEarth),
        series = id(record = series4),
    )
    val person5 = Person(
        id = "5",
        firstName = "Finn",
        lastName = "The Human",
        country = id(record = countryCandy),
        series = id(record = series5),
    )
    val person10 = Person(
        id = "10",
        firstName = "Rimuru",
        lastName = "Tempest",
        country = id(record = countryTempest),
        series = id(record = series6),
    )
    val person15 = Person(
        id = "15",
        firstName = "Desir",
        lastName = "Herrman",
        country = id(record = countryHebrion),
        series = id(record = series7),
    )
    val person20 = Person(
        id = "20",
        firstName = "Shin",
        lastName = "Nouzen",
        country = id(record = countrySanMagnolia),
        series = id(record = series8),
    )
    val person21 = Person(
        id = "21",
        firstName = "Spike",
        lastName = "Spiegel",
        country = id(record = countryMars),
        series = id(record = series9),
    )
}
