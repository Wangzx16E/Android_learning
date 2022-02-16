package ru.threeguns.network.requestor;

import kh.hyper.network.annotations.Adapter;
import kh.hyper.network.annotations.Generator;
import kh.hyper.network.annotations.Host;
import kh.hyper.network.annotations.HttpMethod;
import kh.hyper.network.annotations.Parser;
import kh.hyper.network.annotations.TimeOut;
import ru.threeguns.network.TGNetworkAdapter;
import ru.threeguns.network.TGRequestGenerator;
import ru.threeguns.network.TGResultParser;

@Generator(TGRequestGenerator.class)
@Adapter(TGNetworkAdapter.class)
@Parser(TGResultParser.class)
@HttpMethod("POST")
@Host("{TG_HOST_ADDRESS}")
@TimeOut(20)
public interface TGApi {
}
