# Project Guidelines

> some of the coding conventions (especially naming related) will conflict with language standards. this is intentional.
>
> always prefer explicit wording instead of abbreviations and one-letters. there is no need for them as we have enough storage for them all.

name-case = first letter uppercase, all else lowercase.

## Coding Styles

naming conventions:
- constants (compile-time constants) are **SCREAMING_SNAKE_CASE**;
- variables are **snake_case**;
  - booleans start with **is**/**has**/**can**/**should**;
  - unused/ignored parameters are **_**;
- function/method names are **camelCase**;
  - if a function/method returns a boolean, the function/method name should follow the boolean variable naming convention;
- class names are **PascalCase**;
- type parameters (generics) start with **Type** and one letter identifier (**TypeT**);
- acronyms are treated as regular words: lowercase if part of a variable or start of a function/method, name-case if part of a class name (**UrlHttpId**, **id_name**, **idByName**);
  
filenames:
- source files containing a class/type match that type's name exactly (**PascalCase**);
- all other files (scripts, configs, resources, docs) use **kebab-case**;
- no spaces in filenames, ever;
- root-level project docs (**README**, **LICENSE**, **CONTRIBUTING**, **CHANGELOG**) are **SCREAMING_SNAKE_CASE**, no extension issues aside.

spacing conventions:
- between top level components of the file (classes, imports and first class, functions) the spacing is 3 newlines.
- going down a level (inside the classes), the method spacing becomes 2 newlines.
- inside functions, between logical blocks, the spacing is 1 newline.
- always use spaces between arithmetical operators.
- always use spaces between binary operators / ranges.
- do not use spaces between unary operators.

> anything that requires more than 3 logical nesting levels needs to be reformatted.

formatting conventions:
- braces are on newline;
- code blocks always use braces (no *if* statement with one command not inside braces);
- no line length, but only one statement per line (do not use *if* statements with returns on the same line);
- 4-space indentation;
- trailing commas in multiline lists/collections;
- always leave a blank line at the end of a file;

structure conventions:
- single responsibility per function;
- early returns instead of nested *if*s;
- if a function does not fit on the screen, split it;

## Repo

branching:
- trunk-based branching, each code change lives on its branch until done, then gets merged into **main**.
- branch names start with one of **feature/**, **fix/**, **chore/** and a 2-5 word description of the work done on it.
  
commits:
- each commit follows the pattern **type: description**, where the type is one of **feature**, **fix**, **refactor**, **docs**, **test**, **chore**, **style**, **performance**, and the description is 5-10 words long.
- when the changes require, use the commit body to explain why the changes happened, not what changed.
- one logical change per commit; **WIP**, **fix typo** get squashed.
- use imperative verbs in commit messages (**add feature** instead of *added feature*).
