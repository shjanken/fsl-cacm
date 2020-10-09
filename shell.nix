{ pkgs ? import <nixpkgs> {} }:
with pkgs;
mkShell {
    buildInputs = [ boot jdk14 ];
}
