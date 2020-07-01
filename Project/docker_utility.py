import sys
import subprocess
import os
from pathlib import Path

""" 
This script can take 3 options:
-bp -> build project
-bd -> build docker image
-r OR --run-compose  -> run docker-compose

When no options are given all of the above functions will be executed
"""

# Global variables
# Change as needed
query_xml_path = ""
output_path = ""

# Root of the project
# Mind that the script should be located at the root of the project since project_dir is relative to the file's path
project_dir = Path(__file__).resolve().parent

# Path of some files
docker_compose_yml = project_dir / "docker" / "docker-compose.yml"
tar_path = project_dir / "docker" / "docker_tar"


def build_docker(docker_file, tag, project_dir):
    subprocess.run(
        ["docker", "build", "--pull", "--rm", "-f", docker_file, "-t", tag, project_dir])


def build_project(path_to_pom_dir):
    subprocess.run(["mvn", "-f", path_to_pom_dir, "clean", "package"])


def run_docker_compose(path_to_yml):
    subprocess.run(["docker-compose", "-f", path_to_yml, "up"])


def save_docker_image(image, tar_path):
    subprocess.run(["docker", "save", image, "-o", tar_path])


def main():
    opts = [opt for opt in sys.argv[1:] if opt.startswith("-") or opt.startswith("--")]
    # args = [opt for opt in sys.argv[1:] if not opt.startswith("-")]

    if len(opts) == 0:
        build_project(str(project_dir))
        build_docker("docker/Dockerfile", "argssearch", str(project_dir))
        run_docker_compose(docker_compose_yml)

    if not tar_path.exists():
        os.makedirs(tar_path)

    if "-bp" in opts:
        build_project(str(project_dir))

    if "-bd" in opts:
        build_docker("docker/Dockerfile", "argssearch", str(project_dir))

    if "-r" or "--r" in opts:
        run_docker_compose(docker_compose_yml)


if __name__ == "__main__":
    main()
