
all: 	
	# javac java/Main.java -d ./build -h ./cpp
	javac ../java/app/src/main/java/org/example/FNETLib.java -d ./build -h ./cpp
	g++ -I/usr/lib/jvm/java-21-openjdk/include -I/usr/lib/jvm/java-21-openjdk/include/linux  -fPIC  cpp/lib.cpp -shared -o ./build/nativeLib.o -Wl,-soname -Wl,--no-whole-archive	

clean:
	rm -rf build/*

