package com.helger.jcodemodel.target.record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jdk.jfr.Description;
import jdk.jfr.Experimental;

/**
 * Showcase of the records functionality in java 15 with --enable-preview.
 *
 * @author glelouet {@literal <guillaume.lelouet@gmail.com>}
 *
 */

// Canonical constructor declaration. This declaration defines the full state of the object.
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
	public record House(String name, String address, int rooms) implements Liveable {

		public House {
			Objects.requireNonNull(address);
			if (rooms < 1) {
				throw new UnsupportedOperationException("can't have a house with less than one room");
			}
		}

		public House(String address) {
			this(null, address, 1);
		}

		public House increaseSize() {
			return new House(name, address, rooms + 1);
		}

		public House increaseSize(int addedRooms) {
			return new House(name, address, rooms + addedRooms);
		}

	}

	// we can define generic records
	// also annotations. annotation on the canonical constructor are propagated to
	// the fields and methods.
	@Experimental
	public record LandOwner<T extends Liveable> (
			@Deprecated Person person,
			@Description("empty") T owned){

		// here we deprecated the canonical constructor body
		@Deprecated
		public LandOwner {

		}

		public void callToDeprecation() {
			// TODO this one should be deprecated but is not ?
			person();
			// this one is deprecated since the canonical constructor body is
			// annotated as such.
			new LandOwner<>(null, null);

		}

		// we can of course annotate the instance methods.
		@Deprecated
		public void deprecated() {

		}

	}

	//
	// local records
	//

	public static String runFamily() {
		String ret = "";
		// here we create a new local record
		record Family(Person father, Person mother, List<Person> children) {

			// we redefine the canonical constructor to modify the parameter we store
			public Family(Person father, Person mother, List<Person> children) {
				this.father = father;
				this.mother = mother;
				this.children = List.copyOf(children);
				Objects.requireNonNull(father);
				Objects.requireNonNull(mother);
			}

			// when canonical constructor is redefined, we can't also change its body
			// : this is forbidden
			// public Family{
			// Objects.requireNonNull(father);
			// Objects.requireNonNull(mother);
			// }

			// and another constructor
			public Family(Person father, Person mother) {
				this(father, mother, Collections.emptyList());
			}

			// and another one
			public Family(Person father, Person mother, Person firstBorn, Person...otherChildren) {
				this(father, mother,
						Stream.concat(Stream.of(firstBorn), otherChildren == null ? Stream.empty() : Stream.of(otherChildren))
						.collect(Collectors.toList()));
			}

			// create a modification with a child
			public Family withChild(Person newBorn) {
				List<Person> children = new ArrayList<>(this.children);
				children.add(newBorn);
				return new Family(father, mother, children);
			}

		}

		Person p1 = new Person("p1"), p2 = new Person("p2");
		Family newCouple = new Family(p1, p2);
		// should be 0
		ret += "newCouple has " + newCouple.children().size() + " ; ";
		Family withOneChild = new Family(p1, p2, new Person("p3"));
		Family withTwoChildren = withOneChild.withChild(new Person("p4"));
		// should be 2
		ret += "withTwoChildren has " + withTwoChildren.children().size();

		return ret;
	}

	//
	// tests
	//

	public static String runCreateChild() {
		Person p1 = new Person("P1 P1son", "A1", 9000);
		Person p2 = new Person("P2 P2son", "A2", 1);
		return "the son of " + p1 + " and " + p2 + " is " + p1.childWith(p2, "P3").toString();
	}

	public static void main(String[] args) {
		// the son of Person[name=P1 P1son, address=A1, value=9000] and
		// Person[name=P2 P2son, address=A2, value=1] is Person[name=P3 P1son,
		// address=A1, value=9001]
		System.out.println(runCreateChild());

		// newCouple has 0 ; withTwoChildren has 2
		System.out.println(runFamily());
	}


}
