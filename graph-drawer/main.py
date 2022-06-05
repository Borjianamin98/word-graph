import os
import webbrowser
from io import StringIO

import pandas
import requests
from pyvis.network import Network


def start():
    result_file_path = "/graph/result.csv"
    hdfs_web_api_address = "http://localhost:50075/webhdfs/v1"
    hdfs_web_api_query_params = "op=OPEN&user.name=root&namenoderpcaddress=namenode:9000&offset=0"

    req = requests.get(f"{hdfs_web_api_address}{result_file_path}?{hdfs_web_api_query_params}")
    df = pandas.read_csv(StringIO(req.text))
    selected_records = df.sort_values("total_count", ascending=False).head(100)
    print("Result graph downloaded completely.")

    net = Network(height="100%", width="100%")
    for _, record in selected_records.iterrows():
        net.add_nodes([record["from"], record["to"]])
        net.add_edge(record["from"], record["to"], weight=record["total_count"])

    net.show_buttons(filter_=['physics'])
    network_save_path = "word-graph.html"
    net.write_html(network_save_path)
    webbrowser.open(os.path.abspath(network_save_path))


if __name__ == '__main__':
    start()
