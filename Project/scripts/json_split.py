#!/usr/bin/env python3

import sys
import json
import os

"""
Usage:
1. Arg: Chunk size
2. Arg: Path to input Json
3. Arg: Output directory

e.g. 
    python json_split.py 5 my_json_file.json jsons_dir


"""


def chunks(arr, n):
    """Yield n number of striped chunks from l."""
    for i in range(0, n):
        yield arr[i::n]


args = sys.argv

if len(args) != 4:
    sys.stderr.write("Please specify the number of chunks, source json and the output dir.")
    exit(1)

# User input
chunk_size = int(args[1])
source = str(args[2])
dest = str(args[3])

if not os.path.exists(source):
    sys.stderr.write(f"File {source} does not exist.")

# Read Json input file and split it into chunks
with open(source, "r") as input_file:
    arg_list = json.load(input_file)["arguments"]
    print(f"Arguments Count: {len(arg_list)}")
    arg_chunks = list(chunks(arg_list, chunk_size))

# Create json dir if not exists
# mkdir raises a FileExistsError when folder already exists, so we ignore it.
try:
    os.mkdir(dest)
except FileExistsError:
    pass

# Write chunks to files
count = 1
for chunk in arg_chunks:
    print(f"Creating Json: {count} with {len(chunk)} Arguments.")
    with open(dest + "/json" + str(count) + ".json", "w") as f:
        (lambda x: json.dump({"arguments": x}, f, indent=4))(chunk)
        count = count + 1
