#include <iostream>
#include <string>

int main(int argc, char* argv[]) {
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " <number1> <number2>" << std::endl;
        return 1;
    }

    try {
        int a = std::stoi(argv[1]);
        int b = std::stoi(argv[2]);
        std::cout << a + b << std::endl;
    } catch (const std::invalid_argument& e) {
        std::cerr << "Error: Invalid number format." << std::endl;
        return 1;
    } catch (const std::out_of_range& e) {
        std::cerr << "Error: Number out of range." << std::endl;
        return 1;
    }

    return 0;
}
