package dev.henkle.surreal.errors

class EmptyArgListException(function: String) : SurrealSDKArgumentsException("$function must receive at least one value in its collection argument but an empty collection was found!")
