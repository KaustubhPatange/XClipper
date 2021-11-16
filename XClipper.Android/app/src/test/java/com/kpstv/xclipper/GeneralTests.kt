package com.kpstv.xclipper

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Assert
import org.junit.Test
import kotlin.system.measureTimeMillis

class GeneralTests {

    data class Person(private val name: String)

    @Test
    fun assertCheckClass() {
        val person1 = Person("John")
        val person2 = Person("Hannah")

        if (person1.javaClass == Person::class.java) {
            println("Classes are same")
        }else {
            println("Classes are different")
        }
    }

    @Test
    fun crossInlineTest() {
        println("Working now")
        preMethod {
            println("Executing")
            return@preMethod
        }
        println("Completed")
    }

    private fun preMethod(block: () -> Unit) {
        method { block.invoke() }
    }

    private inline fun method(crossinline block: () -> Unit) {
        block.invoke()
        println("Below code")
    }

    @Test
    fun assertRegexMatchTest() {

        val seconds= measureTimeMillis {
            val string1 = """
            <!DOCTYPE html>
            <html>
            	<body>
            		<script type="text/javascript">
            		function unescapeHtml(escaped_str) {
            		    var div = document.createElement('div');
            		    div.innerHTML = escaped_str;
            		    var child = div.childNodes[0];
            		    return child ? child.nodeValue : null;
            		}
            		
            		function validateProtocol(url) {
            			var parser = document.createElement('a');
            		    parser.href = url;
            		    var protocol = parser.protocol.toLowerCase();
            			if ([ 'javascript:',  'vbscript:',  'data:', 'ftp:',':' , ' '].indexOf(protocol) < 0) {
            				return url;
            			}
            			return null;
            		}
            		
            		function validate(url) {
            			var unescaped_value = unescapeHtml(url);
            			if (unescaped_value && validateProtocol(unescaped_value)) {
            				return unescaped_value;
            			}
            			return '/';
            		}			var hasURI = false;
            			var intervalExecuted = false;
            			window.onload = function() {
            					document.getElementById("l").src = validate("p/1549acfc3f4b?link_click_id=849539774561987418");

            					window.setTimeout(function() {
            						if (!hasURI) {
            							window.top.location = validate("https://medium.com/@ashisjena.talk2u/take-your-skills-to-the-next-level-with-kotlin-1549acfc3f4b?_branch_match_id=849539774561987418");
            						}
            						intervalExecuted = true;
            					}, 300);
            			};

            			window.onblur = function() {
            				hasURI = true;
            			};

            					window.onfocus = function() {
            						if (hasURI) {
            							window.top.location = validate("https://medium.com/@ashisjena.talk2u/take-your-skills-to-the-next-level-with-kotlin-1549acfc3f4b?_branch_match_id=849539774561987418");
            						} else if (intervalExecuted) {
            							window.top.location = validate("https://medium.com/@ashisjena.talk2u/take-your-skills-to-the-next-level-with-kotlin-1549acfc3f4b?_branch_match_id=849539774561987418");
            						}
            					};

            		</script>
            		<iframe id="l" width="1" height="1" style="visibility:hidden"></iframe>
            	</body>
            </html>
        """.trimIndent()

            val URL_PATTERN_REGEX =
                "https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)"
            val string = "https://medium.com/@ashisjena.talk2u/take-your-skills-to-the-next-level-with-kotlin-1549acfc3f4b?_branch_match_id=849539774561987418"

            println(URL_PATTERN_REGEX
                .toRegex().find(string1)?.value)
        }
        println("Time took: $seconds")
    }

    @Test
    fun regexTest() {
        val string = """
            <!DOCTYPE html>
            <html>
            	<body>
            		<script type="text/javascript">
            		function unescapeHtml(escaped_str) {
            		    var div = document.createElement('div');
            		    div.innerHTML = escaped_str;
            		    var child = div.childNodes[0];
            		    return child ? child.nodeValue : null;
            		}
            		
            		function validateProtocol(url) {
            			var parser = document.createElement('a');
            		    parser.href = url;
            		    var protocol = parser.protocol.toLowerCase();
            			if ([ 'javascript:',  'vbscript:',  'data:', 'ftp:',':' , ' '].indexOf(protocol) < 0) {
            				return url;
            			}
            			return null;
            		}
            		
            		function validate(url) {
            			var unescaped_value = unescapeHtml(url);
            			if (unescaped_value && validateProtocol(unescaped_value)) {
            				return unescaped_value;
            			}
            			return '/';
            		}			var hasURI = false;
            			var intervalExecuted = false;
            			window.onload = function() {
            					document.getElementById("l").src = validate("p/1549acfc3f4b?link_click_id=849539774561987418");

            					window.setTimeout(function() {
            						if (!hasURI) {
            							window.top.location = validate("https://medium.com/@ashisjena.talk2u/take-your-skills-to-the-next-level-with-kotlin-1549acfc3f4b?_branch_match_id=849539774561987418");
            						}
            						intervalExecuted = true;
            					}, 300);
            			};

            			window.onblur = function() {
            				hasURI = true;
            			};

            					window.onfocus = function() {
            						if (hasURI) {
            							window.top.location = validate("https://medium.com/@ashisjena.talk2u/take-your-skills-to-the-next-level-with-kotlin-1549acfc3f4b?_branch_match_id=849539774561987418");
            						} else if (intervalExecuted) {
            							window.top.location = validate("https://medium.com/@ashisjena.talk2u/take-your-skills-to-the-next-level-with-kotlin-1549acfc3f4b?_branch_match_id=849539774561987418");
            						}
            					};

            		</script>
            		<iframe id="l" width="1" height="1" style="visibility:hidden"></iframe>
            	</body>
            </html>
        """
        println("https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&//=]*)".toRegex().matchEntire(string)?.value)
    }

    @Test
    fun eventStreamTest() {
        val threshold = 500
        val channel = Channel<Int>()
        GlobalScope.launch {
            channel.send(0)
            delay(50)
            channel.send(2)
            channel.send(3)
            delay(100)
            channel.send(4)
            channel.send(5)
            channel.send(6)
            delay(100)
            channel.send(7)
            delay(600)
            channel.send(8)
            channel.send(9)
            delay(100)
            channel.send(10)
            delay(400)
            channel.close()
        }
        runBlocking {
            val firstList = ArrayList<Int>()
            val secondList = ArrayList<Int>()
            var seconds = System.currentTimeMillis()
            var currentList = firstList
            channel.receiveAsFlow().collect {
                if (System.currentTimeMillis() - seconds <= threshold) {
                    seconds = System.currentTimeMillis()
                    println("Adding: $it")
                    currentList.add(it)
                }else {
                    println("Creating a new list... flushing")
                    currentList = secondList
                    seconds = System.currentTimeMillis()
                    println("Adding: $it")
                    currentList.add(it)
                }
            }
            Assert.assertEquals(firstList, listOf(0,2,3,4,5,6,7))
            Assert.assertEquals(secondList, listOf(8,9,10))
        }
    }

    @Test
    fun transactionDelayedTest() {
        val list = ArrayList<Int>()
        val transactionFlow = MutableSharedFlow<Boolean>()
        GlobalScope.launch {
            for(i in 0..10) {
                delay((80 * i).toLong())
                transactionFlow.emit(true)
                addToTransaction(i)
            }
        }
        val handler = CoroutineExceptionHandler {_, e ->
            println("Cancelled: $e")
        }

        val job = SupervisorJob()
        runBlocking(job + handler) {
            delay(5000)
//            launch {
//                transactionFlow.debounce(500).collect {
//                    if (!it) return@collect
//                    if (list.size == 1) {
//                        println("Single Item: $list")
//                    } else if (list.size > 1) {
//                        println("Multiple Items: $list")
//                    }
//                    list.clear()
//                    transactionFlow.emit(false)
//                    job.cancel()
//                }
//            }
        }
    }
    private val list = ArrayList<Int>()
    private val job = SupervisorJob()
    private val exceptionHandler = CoroutineExceptionHandler {_,_ ->
        // do nothing
    }
    fun addToTransaction(clip: Int) {
        job.cancel()
        list.add(clip)
        CoroutineScope(Dispatchers.IO + job).launch(exceptionHandler) {
            delay(500)
            if (list.size == 1) {
                println("Single Item: $list")
            } else if (list.size > 1) {
                println("Multiple Items: $list")
            }
            list.clear()
            job.cancel()
        }
    }
}