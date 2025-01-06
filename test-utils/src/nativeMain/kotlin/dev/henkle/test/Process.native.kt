package dev.henkle.test

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.refTo
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import platform.posix.FILE
import platform.posix.NULL
import platform.posix.SIGTERM
import platform.posix.execl
import platform.posix.exit
import platform.posix.fgets
import platform.posix.fork
import platform.posix.getpid
import platform.posix.pclose
import platform.posix.popen
import platform.posix.waitpid

@OptIn(ExperimentalForeignApi::class)
actual class Process actual constructor(command: String) {
    private val scope = CoroutineScope(Dispatchers.IO)
    actual val stdout = StringBuilder()
    actual val stderr = StringBuilder()
    private val _output = MutableSharedFlow<String>(replay = 20)
    actual val output: Flow<String> = _output
    private val pidToKill = CompletableDeferred<Int>()
    private val processJob = scope.async {
        val pid = fork()
        // child process will be 0
        val code = if (pid == 0) {
            pidToKill.complete(getpid())
            val fp: CPointer<FILE>? = popen(command, "r")
            val buffer = ByteArray(4096)

            if (fp != null) {
                throw Exception("Unable to execute command!")
            } else {
                var scan = fgets(buffer.refTo(index = 0), buffer.size, fp)
                var line: String
                if (scan != null) {
                    while (scan != NULL) {
                        line = scan!!.toKString()
                        stdout.append(line)
                        _output.emit(line)
                        println(scan.toKString())
                        scan = fgets(buffer.refTo(0), buffer.size, fp)
                    }
                }
                exit(pclose(fp))
            }
            0
        } else {
            memScoped {
                val childReturnCode = alloc<IntVar>()
                // pid 0 awaits any child process whose process group ID is equal to the calling process
                // passing 0 as options selects no options
                waitpid(0, childReturnCode.ptr, 0)
                childReturnCode.value
            }
        }
        code to this@Process.output
    }

    actual suspend fun kill() {
        platform.posix.kill(pidToKill.await(), SIGTERM)
        scope.cancel()
    }

    actual suspend fun wait(): ProcessOutput =
        processJob.await().let { (code, stdout) ->
            ProcessOutput(code = code, stdout = stdout.toString(), stderr = "")
        }
}
