package com.helger.jcodemodel.target.record;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// Canonical constructor declaration
public record Person(String name, String address, int value) {

	// Canonical constructor body. Must have at least same visibility as canonical
	// constructor declaration
	public Person {
		Objects.requireNonNull(name);
	}

	// non canonical constructor. It must start with a call to the canonical
	// constructor
	public Person(String name) {
		this(name, null, 0);
	}

	public Person(String name, int value) {
		this(name, null, value);
	}

	// create an instance method that refer to the field used in the cannonic
	// constructor

	public String firstName() {
		return name.split(" ")[0];
	}
	public String familyName() {
		return Stream.of(name.split(" ")).skip(1).collect(Collectors.joining(" "));
	}

	//
	// utility method
	//

	/**
	 * have a child with another person
	 *
	 * @param other
	 * @param name
	 * @return
	 */
	public Person childWith(Person other, String name) {
		Person inherit = value() > other.value || value == other.value && name.compareToIgnoreCase(other.name) < 0 ? this
				: other;
		return new Person(name + " " + inherit.firstName() + "son", inherit.address, value + other.value);
	}

	//
	// singleton
	//

	private static Person nobody = new Person("nobody");

	public static Person nobody() {
		return nobody;
	}

	//
	// sub classes
	//

	// we can define a sub interface.
	public interface Liveable {
		public int rooms();
	}

	// we can define sub records. Records can implement interfaces, methods
	// deduced by the canonical constructor params are automatically created
	public record house(String name, String address, int rooms) implements Liveable {

		public house {
			Objects.requireNonNull(address);
			if (rooms < 1) {
				throw new UnsupportedOperationException("can't have a house with less than one room");
			}
		}

		public house(String address) {
			this(null, address, 1);
		}

	}

}
