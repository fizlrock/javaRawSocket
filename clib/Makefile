
all: 	
	javac java/Main.java -d ./build -h ./cpp
	g++ -I/usr/lib/jvm/java-21-openjdk/include -I/usr/lib/jvm/java-21-openjdk/include/linux  -fPIC  cpp/lib.cpp -shared -o ./build/nativeLib.o -Wl,-soname -Wl,--no-whole-archive	
	sudo java -Djava.library.path=/home/fizlrock/code2/university/interfaces/lab34/build -cp build Main

clean:
	rm -rf build/*

