package com.twittersfs.server.exceptions;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
@RequiredArgsConstructor
@Data
public class ErrorResponse {
    private final List<String> errorMessages;
}
