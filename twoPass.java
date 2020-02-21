import java.util.*;

public class twoPass {
	public static void main(String[] args) {
		Hashtable<Integer, String> usedNotDefined = new Hashtable<Integer, String>();
		ArrayList<Integer> absoluteAddressExceeded = new ArrayList<>();
		ArrayList<Integer> addressesMultiplyUsed = new ArrayList<>();
		HashSet<String> variableMultiplyDefined = new HashSet<>();
		ArrayList<Module> mods = processInput(addressesMultiplyUsed, variableMultiplyDefined);
//		After these modules are created, iterate through the def list 
//		of all of them and add the base address to generate a sym table
		ArrayList<String> exceededValues = checkAddressOutOfBounds(mods);
		Hashtable<String, Integer> symList = passOne(mods);
		ArrayList<Integer[]> memMap = passTwo(mods, symList, absoluteAddressExceeded, usedNotDefined);
		
//		for (int i = 0; i < memMap.size(); i++) {
//			System.out.println(Integer.toString(memMap.get(i)[0]) + " " + 
//		Integer.toString(memMap.get(i)[1]));
//		}
		System.out.println(printOutput(symList, memMap, exceededValues, addressesMultiplyUsed,
				absoluteAddressExceeded, usedNotDefined, variableMultiplyDefined));
		definedButNotUsed(mods, symList);

	}
	
	public static ArrayList<String> checkAddressOutOfBounds(ArrayList<Module> mods) {
		ArrayList<String> selectedValues = new ArrayList<>();
		for (int i = 0; i < mods.size(); i++) {
			Module currentMod = mods.get(i);
			Hashtable<String, Integer> defs = currentMod.defList;
			int maxPossibleValue = currentMod.number_of_addresses - 1;
			Set<String> keys = defs.keySet();
			for(String key : keys) {
				if (defs.get(key) > maxPossibleValue) { 
					selectedValues.add(key); 
					defs.put(key, maxPossibleValue);
				}
			}
		}
		return selectedValues;
	}
	
	public static void  definedButNotUsed(ArrayList<Module> mods, 
			Hashtable<String, Integer> symList) {
		Set<String> keys = symList.keySet();
		for(String key : keys) {
			Boolean used = false;
			for (int i = 0; i < mods.size(); i++) {
				if (mods.get(i).uses.containsKey(key)) {
					used = true;
					break;
				}
			}
			if (!used) {
//				Find where the variable was defined
				int moduleNumber = -1;
				for (int i = 0; i < mods.size(); i++) {
					if (mods.get(i).defList.containsKey(key)) {
						moduleNumber = i;
						break;
					}
				}
				System.out.println("Warning: " + key + 
						" was defined in module " + Integer.toString(moduleNumber) + 
						" but never used.");
			}
		}
	}
	
	public static String printOutput(Hashtable<String, Integer> symList, 
			ArrayList<Integer[]> memMap, ArrayList<String> exceededValues, 
			ArrayList<Integer> addressesMultiplyUsed, 
			ArrayList<Integer> absoluteAddressExceeded, 
			Hashtable<Integer, String> usedNotDefined, 
			HashSet<String> variableMultiplyDefined) {
		String output = "";
		output += "Symbol Table" +"\n";
		Set<String> keys = symList.keySet();
		for(String key: keys) {
			output += key + "=" + symList.get(key);
			if (exceededValues.contains(key)) {
				output += " Error: Definition exceeds module size; last word in module used.";
			}
			if (variableMultiplyDefined.contains(key)) {
				output += " Error: This variable is multiply defined; last value used.";
			}
			output += "\n";
		}
		output += "\n";
		output += "Memory Map\n";
		for (int i = 0; i < memMap.size(); i++) {
			output += Integer.toString(memMap.get(i)[0]) + ":  " + 
					Integer.toString(memMap.get(i)[1]);
			if (addressesMultiplyUsed.contains(memMap.get(i)[0])) {
				output += " Error: Multiple variables used in instruction; all but last ignored.";
			}
			if (absoluteAddressExceeded.contains(memMap.get(i)[0])) {
				output += " Error: Absolute address exceeds machine size; largest legal value used.";
			}
			if (usedNotDefined.containsKey(memMap.get(i)[0])) {
				output += " Error: " + usedNotDefined.get(memMap.get(i)[0]) +
						" is not defined; 111 used."; 
			}
			output += "\n";
		}
		
		return output;
		
	}
	
	public static ArrayList<Integer[]> passTwo(ArrayList<Module> mods, 
			Hashtable<String, Integer> symList, ArrayList<Integer> absoluteAddressExceeded, 
			Hashtable<Integer, String> usedNotDefined) {
		ArrayList<Integer[]> output = new ArrayList<>();
		int masterCounter = 0;
		for(int i = 0; i < mods.size(); i++) {
			Module currentMod = mods.get(i);
			Integer[] addressList = currentMod.listOfAddresses;
			
			for (int j = 0; j < addressList.length; j++) {
				String currentAddress = addressList[j].toString();
				Integer[] toAdd = new Integer[2];
				toAdd[0] = masterCounter;
//				You may have to check here if the substring is correct
				int lastDigit = Integer.parseInt(currentAddress.substring(4,5));
				if (lastDigit == 1) {
					toAdd[1] = Integer.parseInt(currentAddress.substring(0,4));
				}
				else if (lastDigit == 2) {
					int middleThree = Integer.parseInt(currentAddress.substring(1, 4));
					if (middleThree > 299) {
						absoluteAddressExceeded.add(masterCounter);
						toAdd[1] = Integer.parseInt(currentAddress.substring(0,1) + "299");
					}
					else {
						toAdd[1] = Integer.parseInt(currentAddress.substring(0,4));
					}	
				}
				else if (lastDigit == 3) {
					int compute = Integer.parseInt(currentAddress.substring(0,4));
					int absAddress = compute + currentMod.base_address;
					toAdd[1] = absAddress;
				}
				else {
//					J is the position in the address list
//					First, search the uses list for the variable that applies
					String symbol = "";
					Set<String> keys = currentMod.uses.keySet();
					for(String key: keys) {
						Integer[] array = currentMod.uses.get(key);
						if (Arrays.asList(array).contains(j)) {
							symbol = key;
						}
					}
					String middleThree;
					if (symList.get(symbol) == null) {
						usedNotDefined.put(masterCounter, symbol);
						middleThree = "111";
					}
					else {
						middleThree = String.format("%03d", 
								symList.get(symbol));
					}
					middleThree = currentAddress.substring(0,1) + middleThree;
					int middleThreeInteger = Integer.parseInt(middleThree);
					toAdd[1] = middleThreeInteger;
				}
				output.add(toAdd);
//				Increment the master counter 
				masterCounter++;
			}
		}
		return output;
	}
	
	public static Hashtable<String, Integer> passOne(ArrayList<Module> mods) {
		Hashtable<String, Integer> output = new Hashtable<String, Integer>();
		for(int i = 0; i < mods.size(); i++) {
			Module currentMod = mods.get(i);
			int offset = currentMod.base_address;
			Hashtable<String, Integer> defList = currentMod.defList;
			defList.forEach((k, v) -> { 
	            v = v + offset; 
	            output.put(k, v); 
	        });
		}
		return output;
	}
	
	public static ArrayList<Module> processInput(ArrayList<Integer> addressesMultiplyUsed,
			HashSet<String> variableMultiplyDefined) {
		ArrayList<Module> output = new ArrayList<Module>();
//		This will be used to check repeats in the definitions
		Hashtable<String, Integer> masterDefList = new Hashtable<>();
		Scanner scanner = new Scanner(System.in);
		int numberOfModules = scanner.nextInt();
//		Create a base address counter to increment for every module
		int baseAddressCounter = 0;
//		Populate the output by buildng Module objects
		for (int i = 0; i < numberOfModules; i++) {
//			This is the base address we will pass to create the
//			module object
			int current_base = baseAddressCounter;
//			This block handles the definitions 
			int number_of_definitions = scanner.nextInt();
			Hashtable<String, Integer> defList = new Hashtable<>();
			for (int j = 0; j < number_of_definitions; j++) {
				String symbol = scanner.next();
				int location = scanner.nextInt();
//				Handle the case where the symbol is repeated
				if (masterDefList.containsKey(symbol)) {
					variableMultiplyDefined.add(symbol);
					defList.put(symbol, location);
//					Now remove the instances from any previous modules
					for (int p = 0; p < output.size(); p++) {
						if (output.get(p).defList.containsKey(symbol)) {
							output.get(p).defList.remove(symbol);
						}
					}
				}
				else {
					defList.put(symbol, location);
					masterDefList.put(symbol, location);
				}
			}
			
//			This block handles the uses
			Hashtable<String, Integer[]> uses = new Hashtable<>();
//			This will map positions to strings in case of duplicates
			Hashtable<Integer, String> checkMultiple = new Hashtable<>();
			int number_of_uses = scanner.nextInt();
			for (int j = 0; j < number_of_uses; j++) {
				String symbol = scanner.next();
				ArrayList<Integer> ints = new ArrayList<>();
				int pointer = scanner.nextInt();
				while (pointer != -1) {
					if (checkMultiple.containsKey(pointer)) {
//						The symbol that currently owns the address 
						String currentSym = checkMultiple.get(pointer);
						addressesMultiplyUsed.add(pointer + current_base);
//						Iterate through uses and delete the last use
						Set<String> keySet = uses.keySet();
//						This initialization may be a problem
						Integer[] modifiedList = new Integer[2];
						for (String key : keySet) { 
				            if (key.compareTo(currentSym) == 0) {
				            	Integer[] addressList = uses.get(key);
				            	modifiedList = new Integer[addressList.length - 1];
				            	int modifiedListCounter = 0;
				            	for (int p = 0; p < addressList.length; p++) {
				            		if (addressList[p] != pointer) {
				            			modifiedList[modifiedListCounter] = addressList[p];
				            			modifiedListCounter++;
				            		}
				            	}
				            }
				        }
						uses.put(currentSym, modifiedList);
						ints.add(pointer);
						checkMultiple.put(pointer, symbol);
					}
					else {
						ints.add(pointer);
						checkMultiple.put(pointer, symbol);
					}
					pointer = scanner.nextInt();
				}
				Integer[] toAdd = new Integer[ints.size()];
				for (int m = 0; m < ints.size(); m++) {
					toAdd[m] = ints.get(m);
				}
				uses.put(symbol, toAdd);
			}
//			This block handles addresses 
			int number_of_addresses = scanner.nextInt();
//			Here we increment the base address counter by the addresses
//			for the next module, not this one
			baseAddressCounter += number_of_addresses;
			Integer[] listOfAddresses = new Integer[number_of_addresses];
			for (int j = 0; j < number_of_addresses; j++) {
				listOfAddresses[j] = scanner.nextInt();
			}
			
//			Now, we create the Module object
			Module mod = new Module(number_of_definitions, number_of_uses, number_of_addresses,
					current_base, defList, uses, listOfAddresses);
//			Now we add this to the arraylist
			output.add(mod);
		}
		scanner.close();
		return output;
	}
	

}

class Module {
	int number_of_definitions;
	int number_of_uses;
	int number_of_addresses;
	int base_address;
	Hashtable<String, Integer> defList;
	Hashtable<String, Integer[]> uses;
	Integer[] listOfAddresses;
	
	Module (int number_of_definitions, int number_of_uses, int number_of_addresses,
			int base_address, Hashtable<String, Integer> defList, 
			Hashtable<String, Integer[]> uses, Integer[] listOfAddresses) {
		this.number_of_definitions = number_of_definitions;
		this.number_of_uses = number_of_uses;
		this.number_of_addresses = number_of_addresses;
		this.base_address = base_address;
		this.defList = defList;
		this.uses = uses;
		this.listOfAddresses = listOfAddresses;
	}
	
	public String toString() {
		return(defList.toString() + uses.toString() + listOfAddresses.toString());
	}
	
	
	
}
