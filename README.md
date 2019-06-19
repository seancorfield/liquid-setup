# Liquid Configuration

The `liquid.clj` file here is a `.liq` file for configuring the [Liquid](https://github.com/mogenslund/liquid/) in-process editor. If you clone this repo and want to keep it up to date, you can symlink `liquid.clj` to `~/.liq` to avoid repeatedly copying it.

The main functionality added here is [REBL](https://github.com/cognitect-labs/REBL-distro/) support in the form of several evaluate-and-submit-to-REBL commands and their key mappings:

* `c r d` -- show docstring for symbol at point (as a Var),
* `c r D` -- `def` a symbol/expression pair (as found in a `let` binding); this makes debugging easier by allowing you to evaluate nested expressions inside a `let` (although you'll still need to manually `def` arguments from the enclosing function if those expressions reference them),
* `c r n` -- inspect the current namespace in REBL -- shows all the Vars in it,
* `c r r` -- evaluate the selection or sexp at point in REBL,
* `c r v` -- inspect the symbol at point (as a Var) in REBL -- shows what it uses/what uses it.

In addition, two testing commands are added:

* `c t t` -- run the test at point (assumes the cursor is on a symbol that is a test Var),
* `c t x` -- run all tests in the current namespace.

These both display the test output/results in Liquid and in REBL (it's easier to read formatted output in REBL).

In addition `c p p` is overwritten to match `c r r`, just in case you have that as muscle memory and want to use REBL by default (as I do!).
