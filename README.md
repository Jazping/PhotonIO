# Prelude
<p>
Today issue one corresponding framework of java for netty, my intent is try to enlighten or give some idea for java elite developer,
how to constitude a efficent network transportation in protocoled form. the code is preparing, and with little comments on it therefore 
mybe difficult to understand, so-called that just for java elite developer.
</p>

# PhotonIO
<p>
Binary protocoled correspondence norm for netty.
</p>

# Formulation

<p><bold>BIO is one blocking correspondence specification</bold></p>
<p><bold>NIO is one non-blocking multipath reused regulation</bold></p>
<p><bold>AIO is one call backed kind of NIO stipulation</bold></p>

<p>
There is old to new corresponding technique from up to down, BIO as one synchronized
widely applied correspondence regulation, ordinary requests are comply that norm, each
request will blocking until the last request had responded. NIO deem multipath reused and
running paralleled postulate that all requests has no correlation each other. AIO base on NIO
and ameliorated that allowed call back procession according with response through future.
</p>
<p>
Netty is one high performant NIO kind correspondence framework, already sealed some
general protocol like http2 decoder. Of course, newly version support AIO rules.
</p>
<p>
It is necessary to explore some flexible protocoled program develop framework, although
these are not the significant innovation. But http-base application is insufficient after all,
grope one efficient means to improve protocoled program is magnitude.
</p>

# Purpose
<p>
Intent to resolve packet’s tackiness, overlap or losing issues, provide an easy protocoled
program environment, constitute efficient files transmit, chatting apps or message
intermediates.
</p>

# The gist
<p>
Infrastructure constituted by server and client tow parts. Bilateral communication using a
set of request connector, dispatcher, buffer and consumer to detach NIO&PIO. The
connector obligation is establish connection with other server, dispatcher receive and
administer the connection. Each connection will be allocate one unique identification when
established. After handshake with other server, send binary protocoled message through
connector, the message general as a buffer.
</p>

<p>
➢ server side
</p>
<p>
It contain series of registered consumer and message dispatcher. First of all, resolve the
protocol which cling by message from client, then distinguish message by them head and
dispatch them to corresponding consumer prerequisite protocol knew by each other side. If
the server supported dual orientation communication, it contain connector procedure too.
</p>
<p>
➢ client side
</p>
<p>
It similar to server side, if only send commands, that should be without dispatcher.
</p>
<p>
➢ connector
</p>
<p>
Use to establish connection between tow point.
</p>
➢ dispatcher
Use to distribute message to consumer
➢ connection
When point-to-point bridge established, that will generate a connection whit an unique
identification for manage them, every connection can set some properties in sessional
communication. Generally, one client only has one connection retention for one point, be
cause of NIO already take multipath reuse measure.
➢ buffer
Buffer contain binary message lodged in memory temporarily, it can be allocate by virtual
machine or direct in operation system. We recommend direct buffer on sending, heap buffer
on reading. Besides, pooled them whatever type them is.
➢ buffer pool
Buffer pool use to predestine quantitative buffers in memory, hence spare the time which
entailed by buffer create and destroy. There are corresponding pools in different NIO
intermediates, pools are also distinguished direct pool and heap pool.
➢ Consumer
Consumer is developer defined message process procedure, always contain protocol uninstall
and business process code. In generally, one consumer define by visual character sequence
and register to dispatcher.
It is hard to read constant range data packet from socket, thus incur one holistic packet may
be reads several times. So it is hard to protocoled program in current exist technology, PIO
judge data whether spliced through a status which respond by consumer, if data has been
sliced, it will combine next reading then try it again, this procedure loop till one packet is
wholly readied or exception occur. Due to data combinate operation, therefor it is not
proper to process big buffer at one time. Besides, consumer cannot submit execution until
one data packet is holistic. Framework offer basic consumer for connection establish, and
consumer for file transmit. Also purvey ordinary file reading or writing method in client API.
File transition has tow alternative basic protocol, they are TCP and UDP.
To solve the packet losing situation, framework taken idempotent stream measure to ensure
transition availability on UDP protocol. It’s mechanism is resend the fragment which has no
response. Idempotent stream categorize file and byte array tow type. File idempotent
stream for large file transmit, and byte array idempotent stream for small data transmit, this
small data usual memorized.
To solve memory overwhelm issue on transmit big file, PIO take transition relegate strategy.
Framework delay one hundred milliseconds to continue the suspended transition if memory
overflow on transmitting. Be cause of velocity of CPU more faster than network speed, that
is often occur memory overflow on transmitting big file.
Performance determine by network and disk writing ability, file transition will write some
temporary file to disk. If low ability in network or disk, that will throw many data duplicated
exception which will affect performance.
Framework stipulate consumer development, purely develop consumer like developing
controller in spring framework. Add Annotation on one class(including static) as consumer
bean, and add mapping upon method to define one consumer, the method annotation
require a string as name for mapping, parameters of method optional to add specify
annotation, due to primary argument length are constant, so only add annotation to nonprimary 
arguments, specialize parameters also can specify decoder for resolve the uservariable, 
base on annotation developed consumer only require specify parameters order, no
need concern primary parameters parsing. All of these final purpose just for easy, decent
and graceful.

# Conduce

Vest-pocket protocoled program design will efficiently perform upon the strut. Furthermore,
PIO flexible on NIO intermediates, even if you like, also can develop a strut like Netty as
subgrade transmit intermediates.

# Working procedure

Subgrade NIO engine -> buffer -> connection -> dispatcher -> consumer

Data overlap or sliced is solve by dispatcher procedure, then convey to consumer for
complete the rest business.

# Use case

Only one use case that is neuro of Big-Bong block chain.
